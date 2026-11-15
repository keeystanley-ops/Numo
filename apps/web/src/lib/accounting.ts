import { and, desc, eq, gte, sql } from 'drizzle-orm';
import {
  accounts,
  bills,
  budgets,
  debts,
  getDb,
  getPoolDb,
  holdings,
  ledgerEntries,
  savingsGoals,
  transactions,
  transactionSplits,
  users,
} from '@/db';

export interface SplitInput {
  category: string;
  amount: number;
  isBusiness?: boolean;
  notes?: string;
}

export interface RecordTransactionInput {
  userId: string;
  type: 'EXPENSE' | 'INCOME' | 'TRANSFER' | 'INVESTMENT_CONTRIBUTION' | 'DEBT_PAYMENT';
  amount: number;
  currency?: string;
  date?: Date;
  description: string;
  merchant?: string;
  receiptUrl?: string;
  sourceAccountId?: string;
  destinationAccountId?: string;
  debtId?: string;
  principalAmount?: number;
  interestAmount?: number;
  splits?: SplitInput[];
}

/**
 * Enforces PLAN.md Section 5 Double-Entry Bookkeeping Rules inside a single DB transaction.
 */
export async function recordDoubleEntryTransaction(input: RecordTransactionInput) {
  if (input.amount <= 0) {
    throw new Error('Transaction amount must be greater than zero.');
  }

  const poolDb = getPoolDb();
  return poolDb.transaction(async (tx) => {
    const [createdTx] = await tx
      .insert(transactions)
      .values({
        userId: input.userId,
        type: input.type,
        amount: input.amount.toFixed(2),
        currency: input.currency || 'KES',
        date: input.date || new Date(),
        description: input.description,
        merchant: input.merchant || null,
        receiptUrl: input.receiptUrl || null,
      })
      .returning();

    // Ledger & Account balance updates by transaction type
    if (input.type === 'EXPENSE') {
      if (input.sourceAccountId) {
        await tx.insert(ledgerEntries).values({
          transactionId: createdTx.id,
          accountId: input.sourceAccountId,
          entryType: 'CREDIT',
          amount: input.amount.toFixed(2),
        });
        await tx
          .update(accounts)
          .set({ balance: sql`${accounts.balance} - ${input.amount.toFixed(2)}::numeric` })
          .where(and(eq(accounts.id, input.sourceAccountId), eq(accounts.userId, input.userId)));
      }
    } else if (input.type === 'INCOME') {
      const targetId = input.destinationAccountId || input.sourceAccountId;
      if (targetId) {
        await tx.insert(ledgerEntries).values({
          transactionId: createdTx.id,
          accountId: targetId,
          entryType: 'DEBIT',
          amount: input.amount.toFixed(2),
        });
        await tx
          .update(accounts)
          .set({ balance: sql`${accounts.balance} + ${input.amount.toFixed(2)}::numeric` })
          .where(and(eq(accounts.id, targetId), eq(accounts.userId, input.userId)));
      }
    } else if (input.type === 'TRANSFER') {
      // Rule 1: Transfers are NEVER Income or Expenses; paired debit & credit
      if (!input.sourceAccountId || !input.destinationAccountId) {
        throw new Error('Transfers require both sourceAccountId and destinationAccountId.');
      }
      await tx.insert(ledgerEntries).values([
        {
          transactionId: createdTx.id,
          accountId: input.sourceAccountId,
          entryType: 'CREDIT',
          amount: input.amount.toFixed(2),
        },
        {
          transactionId: createdTx.id,
          accountId: input.destinationAccountId,
          entryType: 'DEBIT',
          amount: input.amount.toFixed(2),
        },
      ]);
      await tx
        .update(accounts)
        .set({ balance: sql`${accounts.balance} - ${input.amount.toFixed(2)}::numeric` })
        .where(and(eq(accounts.id, input.sourceAccountId), eq(accounts.userId, input.userId)));
      await tx
        .update(accounts)
        .set({ balance: sql`${accounts.balance} + ${input.amount.toFixed(2)}::numeric` })
        .where(
          and(eq(accounts.id, input.destinationAccountId), eq(accounts.userId, input.userId))
        );
    } else if (input.type === 'INVESTMENT_CONTRIBUTION') {
      // Rule 2: Investment contributions move cash out without inflating living expenses
      if (input.sourceAccountId) {
        await tx.insert(ledgerEntries).values({
          transactionId: createdTx.id,
          accountId: input.sourceAccountId,
          entryType: 'CREDIT',
          amount: input.amount.toFixed(2),
        });
        await tx
          .update(accounts)
          .set({ balance: sql`${accounts.balance} - ${input.amount.toFixed(2)}::numeric` })
          .where(and(eq(accounts.id, input.sourceAccountId), eq(accounts.userId, input.userId)));
      }
    } else if (input.type === 'DEBT_PAYMENT') {
      // Rule 3: Debt payments separate principal reduction vs interest expense
      if (input.sourceAccountId) {
        await tx.insert(ledgerEntries).values({
          transactionId: createdTx.id,
          accountId: input.sourceAccountId,
          entryType: 'CREDIT',
          amount: input.amount.toFixed(2),
        });
        await tx
          .update(accounts)
          .set({ balance: sql`${accounts.balance} - ${input.amount.toFixed(2)}::numeric` })
          .where(and(eq(accounts.id, input.sourceAccountId), eq(accounts.userId, input.userId)));
      }
      const principal = input.principalAmount ?? input.amount;
      if (input.debtId && principal > 0) {
        await tx
          .update(debts)
          .set({
            currentBalance: sql`GREATEST(0, ${debts.currentBalance} - ${principal.toFixed(2)}::numeric)`,
          })
          .where(and(eq(debts.id, input.debtId), eq(debts.userId, input.userId)));
      }
    }

    const splits =
      input.splits && input.splits.length > 0
        ? input.splits
        : [{ category: input.type === 'INCOME' ? 'Income' : 'Other', amount: input.amount }];

    await tx.insert(transactionSplits).values(
      splits.map((s) => ({
        transactionId: createdTx.id,
        category: s.category,
        amount: s.amount.toFixed(2),
        isBusiness: Boolean(s.isBusiness),
        notes: s.notes || null,
      }))
    );

    return createdTx;
  });
}

export async function getDashboardOverview(userId: string) {
  const db = getDb();

  const [userRow, userAccounts, recentTx, userBudgets, userBills, userGoals, userDebts, userHoldings] =
    await Promise.all([
      db.query.users.findFirst({ where: eq(users.id, userId) }),
      db.select().from(accounts).where(and(eq(accounts.userId, userId), eq(accounts.isArchived, false))),
      db
        .select()
        .from(transactions)
        .where(eq(transactions.userId, userId))
        .orderBy(desc(transactions.date))
        .limit(20),
      db.select().from(budgets).where(eq(budgets.userId, userId)),
      db.select().from(bills).where(and(eq(bills.userId, userId), eq(bills.isPaid, false))),
      db.select().from(savingsGoals).where(eq(savingsGoals.userId, userId)),
      db.select().from(debts).where(eq(debts.userId, userId)),
      db.select().from(holdings).where(eq(holdings.userId, userId)),
    ]);

  const availableCash = userAccounts
    .filter((a) => a.type !== 'INVESTMENT_CASH')
    .reduce((sum, a) => sum + Number(a.balance), 0);

  const monthStart = new Date();
  monthStart.setDate(1);
  monthStart.setHours(0, 0, 0, 0);

  const monthTx = await db
    .select()
    .from(transactions)
    .where(and(eq(transactions.userId, userId), gte(transactions.date, monthStart)));

  const monthlyIncome = monthTx
    .filter((t) => t.type === 'INCOME')
    .reduce((sum, t) => sum + Number(t.amount), 0);
  const monthlySpent = monthTx
    .filter((t) => t.type === 'EXPENSE')
    .reduce((sum, t) => sum + Number(t.amount), 0);

  const monthlyBudgetTarget = userBudgets.reduce((sum, b) => sum + Number(b.limitAmount), 0);

  const totalPortfolioValuation = userHoldings.reduce(
    (sum, h) => sum + Number(h.units) * Number(h.currentPrice) * Number(h.fxRateToPrimary),
    0
  );
  const totalInvestedCapital = userHoldings.reduce(
    (sum, h) => sum + Number(h.units) * Number(h.purchasePrice) * Number(h.fxRateToPrimary),
    0
  );
  const totalDebt = userDebts.reduce((sum, d) => sum + Number(d.currentBalance), 0);
  const totalNetWorth = availableCash + totalPortfolioValuation - totalDebt;

  return {
    user: userRow,
    currency: userRow?.primaryCurrency || 'KES',
    availableCash,
    monthlyIncome,
    monthlySpent,
    monthlyBudgetTarget,
    totalRemaining: Math.max(0, monthlyBudgetTarget - monthlySpent),
    totalPortfolioValuation,
    totalInvestedCapital,
    totalPortfolioReturn: totalPortfolioValuation - totalInvestedCapital,
    totalDebt,
    totalNetWorth,
    accounts: userAccounts,
    transactions: recentTx,
    budgets: userBudgets,
    bills: userBills,
    goals: userGoals,
    debts: userDebts,
    holdings: userHoldings,
  };
}

export function calculateDebtStrategy(
  debtRows: Array<{ id: string; name: string; currentBalance: string | number; interestRate: string | number; monthlyPayment: string | number }>,
  extraMonthlyPayment = 0
) {
  const normalized = debtRows
    .map((d) => ({
      id: d.id,
      name: d.name,
      balance: Number(d.currentBalance),
      apr: Number(d.interestRate),
      minPayment: Number(d.monthlyPayment),
    }))
    .filter((d) => d.balance > 0);

  return {
    extraMonthlyPayment,
    avalancheOrder: [...normalized].sort((a, b) => b.apr - a.apr),
    snowballOrder: [...normalized].sort((a, b) => a.balance - b.balance),
  };
}
