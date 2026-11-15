import { z } from 'zod';
import { and, desc, eq, sql } from 'drizzle-orm';
import {
  accounts,
  bills,
  budgets,
  debts,
  getDb,
  holdings,
  investmentActivities,
  savingsGoals,
  transactions,
  users,
  valuationSnapshots,
} from '@/db';
import {
  calculateDebtStrategy,
  getDashboardOverview,
  recordDoubleEntryTransaction,
} from '@/lib/accounting';
import { protectedProcedure, publicProcedure, router } from './init';

export const appRouter = router({
  health: publicProcedure.query(() => ({ ok: true, service: 'numo-trpc-api' })),

  dashboard: router({
    overview: protectedProcedure.query(async ({ ctx }) => {
      return getDashboardOverview(ctx.userId);
    }),
  }),

  onboarding: router({
    complete: protectedProcedure
      .input(
        z.object({
          primaryCurrency: z.string().default('KES'),
          country: z.string().default('KE'),
          preferredEnvironment: z.enum(['MONEY', 'INVESTMENTS']).default('MONEY'),
          startingAccounts: z
            .array(
              z.object({
                name: z.string(),
                type: z.enum(['CASH', 'MPESA', 'BANK', 'SAVINGS', 'INVESTMENT_CASH']),
                balance: z.number().nonnegative(),
              })
            )
            .default([]),
          startingBudgets: z
            .array(
              z.object({
                category: z.string(),
                limitAmount: z.number().positive(),
              })
            )
            .default([]),
        })
      )
      .mutation(async ({ ctx, input }) => {
        const db = getDb();
        await db
          .update(users)
          .set({
            primaryCurrency: input.primaryCurrency,
            country: input.country,
            preferredEnvironment: input.preferredEnvironment,
            onboardingCompleted: true,
            updatedAt: new Date(),
          })
          .where(eq(users.id, ctx.userId));

        if (input.startingAccounts.length > 0) {
          await db.insert(accounts).values(
            input.startingAccounts.map((a) => ({
              userId: ctx.userId,
              name: a.name,
              type: a.type,
              currency: input.primaryCurrency,
              balance: a.balance.toFixed(2),
            }))
          );
        }

        if (input.startingBudgets.length > 0) {
          await db.insert(budgets).values(
            input.startingBudgets.map((b) => ({
              userId: ctx.userId,
              category: b.category,
              limitAmount: b.limitAmount.toFixed(2),
            }))
          );
        }

        return { ok: true };
      }),
  }),

  accounts: router({
    list: protectedProcedure.query(async ({ ctx }) => {
      const db = getDb();
      return db
        .select()
        .from(accounts)
        .where(and(eq(accounts.userId, ctx.userId), eq(accounts.isArchived, false)));
    }),
    create: protectedProcedure
      .input(
        z.object({
          name: z.string().min(1),
          type: z.enum(['CASH', 'MPESA', 'BANK', 'SAVINGS', 'INVESTMENT_CASH']),
          currency: z.string().default('KES'),
          balance: z.number().default(0),
        })
      )
      .mutation(async ({ ctx, input }) => {
        const db = getDb();
        const [created] = await db
          .insert(accounts)
          .values({
            userId: ctx.userId,
            name: input.name,
            type: input.type,
            currency: input.currency,
            balance: input.balance.toFixed(2),
          })
          .returning();
        return created;
      }),
  }),

  transactions: router({
    list: protectedProcedure.query(async ({ ctx }) => {
      const db = getDb();
      return db
        .select()
        .from(transactions)
        .where(eq(transactions.userId, ctx.userId))
        .orderBy(desc(transactions.date))
        .limit(50);
    }),
    create: protectedProcedure
      .input(
        z.object({
          type: z.enum([
            'EXPENSE',
            'INCOME',
            'TRANSFER',
            'INVESTMENT_CONTRIBUTION',
            'DEBT_PAYMENT',
          ]),
          amount: z.number().positive(),
          currency: z.string().default('KES'),
          description: z.string().min(1),
          merchant: z.string().optional(),
          receiptUrl: z.string().optional(),
          sourceAccountId: z.string().uuid().optional(),
          destinationAccountId: z.string().uuid().optional(),
          debtId: z.string().uuid().optional(),
          principalAmount: z.number().optional(),
          interestAmount: z.number().optional(),
          splits: z
            .array(
              z.object({
                category: z.string(),
                amount: z.number().positive(),
                isBusiness: z.boolean().optional(),
                notes: z.string().optional(),
              })
            )
            .optional(),
        })
      )
      .mutation(async ({ ctx, input }) => {
        return recordDoubleEntryTransaction({ ...input, userId: ctx.userId });
      }),
  }),

  budgets: router({
    list: protectedProcedure.query(async ({ ctx }) => {
      const db = getDb();
      return db.select().from(budgets).where(eq(budgets.userId, ctx.userId));
    }),
    upsert: protectedProcedure
      .input(
        z.object({
          category: z.string().min(1),
          limitAmount: z.number().positive(),
          period: z.string().default('MONTHLY'),
          alertThreshold: z.number().int().min(1).max(100).default(80),
        })
      )
      .mutation(async ({ ctx, input }) => {
        const db = getDb();
        const existing = await db
          .select()
          .from(budgets)
          .where(and(eq(budgets.userId, ctx.userId), eq(budgets.category, input.category)));
        if (existing[0]) {
          const [updated] = await db
            .update(budgets)
            .set({
              limitAmount: input.limitAmount.toFixed(2),
              period: input.period,
              alertThreshold: input.alertThreshold,
            })
            .where(eq(budgets.id, existing[0].id))
            .returning();
          return updated;
        }
        const [created] = await db
          .insert(budgets)
          .values({
            userId: ctx.userId,
            category: input.category,
            limitAmount: input.limitAmount.toFixed(2),
            period: input.period,
            alertThreshold: input.alertThreshold,
          })
          .returning();
        return created;
      }),
  }),

  goals: router({
    list: protectedProcedure.query(async ({ ctx }) => {
      const db = getDb();
      return db.select().from(savingsGoals).where(eq(savingsGoals.userId, ctx.userId));
    }),
    create: protectedProcedure
      .input(
        z.object({
          title: z.string().min(1),
          targetAmount: z.number().positive(),
          savedAmount: z.number().nonnegative().default(0),
          monthlyContribution: z.number().nonnegative().default(0),
          runwayMonths: z.number().int().positive().optional(),
        })
      )
      .mutation(async ({ ctx, input }) => {
        const db = getDb();
        const [created] = await db
          .insert(savingsGoals)
          .values({
            userId: ctx.userId,
            title: input.title,
            targetAmount: input.targetAmount.toFixed(2),
            savedAmount: input.savedAmount.toFixed(2),
            monthlyContribution: input.monthlyContribution.toFixed(2),
            runwayMonths: input.runwayMonths ?? null,
          })
          .returning();
        return created;
      }),
    contribute: protectedProcedure
      .input(z.object({ goalId: z.string().uuid(), amount: z.number().positive() }))
      .mutation(async ({ ctx, input }) => {
        const db = getDb();
        const [updated] = await db
          .update(savingsGoals)
          .set({
            savedAmount: sql`${savingsGoals.savedAmount} + ${input.amount.toFixed(2)}::numeric`,
          })
          .where(and(eq(savingsGoals.id, input.goalId), eq(savingsGoals.userId, ctx.userId)))
          .returning();
        return updated;
      }),
  }),

  debts: router({
    list: protectedProcedure.query(async ({ ctx }) => {
      const db = getDb();
      return db.select().from(debts).where(eq(debts.userId, ctx.userId));
    }),
    strategy: protectedProcedure
      .input(z.object({ extraMonthlyPayment: z.number().nonnegative().default(0) }))
      .query(async ({ ctx, input }) => {
        const db = getDb();
        const rows = await db.select().from(debts).where(eq(debts.userId, ctx.userId));
        return calculateDebtStrategy(rows, input.extraMonthlyPayment);
      }),
  }),

  bills: router({
    list: protectedProcedure.query(async ({ ctx }) => {
      const db = getDb();
      return db.select().from(bills).where(eq(bills.userId, ctx.userId));
    }),
  }),

  investments: router({
    portfolio: protectedProcedure.query(async ({ ctx }) => {
      const db = getDb();
      const rows = await db.select().from(holdings).where(eq(holdings.userId, ctx.userId));
      return rows;
    }),
    createHolding: protectedProcedure
      .input(
        z.object({
          assetClass: z.enum([
            'STOCK_NSE',
            'STOCK_GLOBAL',
            'MMF',
            'TBILL',
            'BOND',
            'SACCO',
            'REIT',
            'CRYPTO',
            'REAL_ESTATE',
            'CUSTOM',
          ]),
          name: z.string().min(1),
          ticker: z.string().optional(),
          units: z.number().positive(),
          purchasePrice: z.number().positive(),
          currentPrice: z.number().positive(),
          currency: z.string().default('KES'),
          fxRateToPrimary: z.number().positive().default(1),
          dividendYield: z.number().nonnegative().optional(),
        })
      )
      .mutation(async ({ ctx, input }) => {
        const db = getDb();
        const [created] = await db
          .insert(holdings)
          .values({
            userId: ctx.userId,
            assetClass: input.assetClass,
            name: input.name,
            ticker: input.ticker || null,
            units: input.units.toFixed(4),
            purchasePrice: input.purchasePrice.toFixed(2),
            currentPrice: input.currentPrice.toFixed(2),
            currency: input.currency,
            fxRateToPrimary: input.fxRateToPrimary.toFixed(4),
            dividendYield: input.dividendYield?.toFixed(2) ?? null,
          })
          .returning();
        return created;
      }),
    updateValuation: protectedProcedure
      .input(
        z.object({
          holdingId: z.string().uuid(),
          newPrice: z.number().positive(),
          notes: z.string().optional(),
        })
      )
      .mutation(async ({ ctx, input }) => {
        const db = getDb();
        const [updated] = await db
          .update(holdings)
          .set({
            currentPrice: input.newPrice.toFixed(2),
            lastValuationDate: new Date(),
          })
          .where(and(eq(holdings.id, input.holdingId), eq(holdings.userId, ctx.userId)))
          .returning();
        if (updated) {
          const totalValuation = Number(updated.units) * input.newPrice;
          await db.insert(valuationSnapshots).values({
            holdingId: updated.id,
            valuation: totalValuation.toFixed(2),
            notes: input.notes || 'Manual valuation update',
          });
          await db.insert(investmentActivities).values({
            holdingId: updated.id,
            type: 'VALUATION_UPDATE',
            units: updated.units,
            amount: totalValuation.toFixed(2),
            date: new Date(),
            notes: input.notes || 'Valuation snapshot',
          });
        }
        return updated;
      }),
  }),
});

export type AppRouter = typeof appRouter;
