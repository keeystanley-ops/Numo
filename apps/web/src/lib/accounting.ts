import { and, desc, eq, gte, inArray, sql } from 'drizzle-orm';
import {
  accounts,
  assets,
  billPayments,
  bills,
  budgets,
  debts,
  getDb,
  getPoolDb,
  holdings,
  ledgerEntries,
  liabilities,
  netWorthSnapshots,
  notifications,
  recurringSchedules,
  savingsGoals,
  transactions,
  transactionSplits,
  userCategories,
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
  grossAmount?: number;
  deductionAmount?: number;
  fee?: number;
  currency?: string;
  date?: Date;
  description: string;
  merchant?: string;
  reference?: string;
  receiptUrl?: string;
  sourceAccountId?: string;
  destinationAccountId?: string;
  debtId?: string;
  principalAmount?: number;
  interestAmount?: number;
  splits?: SplitInput[];
  idempotencyKey?: string;
  metadata?: Record<string, unknown>;
}

/**
 * Enforces Double-Entry Bookkeeping Rules inside a single atomic DB transaction
 * with server-side balance validation and idempotency protection.
 */
export async function recordDoubleEntryTransaction(input: RecordTransactionInput) {
  if (input.amount <= 0) {
    throw new Error('Transaction amount must be greater than zero.');
  }
  const fee = Math.max(0, Number(input.fee ?? 0));

  const poolDb = getPoolDb();
  return poolDb.transaction(async (tx) => {
    if (input.idempotencyKey) {
      const existing = await tx
        .select()
        .from(transactions)
        .where(
          and(
            eq(transactions.userId, input.userId),
            eq(transactions.idempotencyKey, input.idempotencyKey)
          )
        )
        .limit(1);
      if (existing.length > 0) {
        return existing[0];
      }
    }

    // Server-side balance validation for outgoing operations
    if (
      input.type === 'EXPENSE' ||
      input.type === 'TRANSFER' ||
      input.type === 'INVESTMENT_CONTRIBUTION' ||
      input.type === 'DEBT_PAYMENT'
    ) {
      if (!input.sourceAccountId) {
        throw new Error('Source account is required.');
      }
      const [srcAcc] = await tx
        .select()
        .from(accounts)
        .where(and(eq(accounts.id, input.sourceAccountId), eq(accounts.userId, input.userId)));
      if (!srcAcc) {
        throw new Error('Source account not found.');
      }
      const requiredDebit = input.amount + (input.type === 'TRANSFER' ? fee : 0);
      if (Number(srcAcc.balance) < requiredDebit) {
        throw new Error(
          `Insufficient balance in ${srcAcc.name}. Available: KES ${Number(srcAcc.balance).toLocaleString()}.`
        );
      }
    }

    if (input.type === 'TRANSFER') {
      if (!input.destinationAccountId) {
        throw new Error('Destination account is required for transfers.');
      }
      if (input.sourceAccountId === input.destinationAccountId) {
        throw new Error('Source and destination accounts cannot be the same.');
      }
    }

    const [createdTx] = await tx
      .insert(transactions)
      .values({
        userId: input.userId,
        type: input.type,
        amount: input.amount.toFixed(2),
        grossAmount: (input.grossAmount ?? input.amount).toFixed(2),
        deductionAmount: (input.deductionAmount ?? 0).toFixed(2),
        fee: fee.toFixed(2),
        currency: 'KES',
        date: input.date || new Date(),
        description: input.description,
        merchant: input.merchant || null,
        reference: input.reference || null,
        receiptUrl: input.receiptUrl || null,
        sourceAccountId: input.sourceAccountId || null,
        destinationAccountId: input.destinationAccountId || null,
        idempotencyKey: input.idempotencyKey || null,
        metadata: input.metadata || null,
      })
      .returning();

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
      const totalDebit = input.amount + fee;
      await tx.insert(ledgerEntries).values([
        {
          transactionId: createdTx.id,
          accountId: input.sourceAccountId!,
          entryType: 'CREDIT',
          amount: totalDebit.toFixed(2),
        },
        {
          transactionId: createdTx.id,
          accountId: input.destinationAccountId!,
          entryType: 'DEBIT',
          amount: input.amount.toFixed(2),
        },
      ]);
      await tx
        .update(accounts)
        .set({ balance: sql`${accounts.balance} - ${totalDebit.toFixed(2)}::numeric` })
        .where(and(eq(accounts.id, input.sourceAccountId!), eq(accounts.userId, input.userId)));
      await tx
        .update(accounts)
        .set({ balance: sql`${accounts.balance} + ${input.amount.toFixed(2)}::numeric` })
        .where(and(eq(accounts.id, input.destinationAccountId!), eq(accounts.userId, input.userId)));
    } else if (input.type === 'INVESTMENT_CONTRIBUTION') {
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
        : [{ category: input.type === 'INCOME' ? 'Income' : input.type === 'TRANSFER' ? 'Transfer' : 'Other', amount: input.amount }];

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

/**
 * Deletes a transaction and atomically reverses its account balance effects.
 */
export async function deleteTransactionAtomic(userId: string, transactionId: string) {
  const poolDb = getPoolDb();
  return poolDb.transaction(async (tx) => {
    const [existing] = await tx
      .select()
      .from(transactions)
      .where(and(eq(transactions.id, transactionId), eq(transactions.userId, userId)));
    if (!existing) {
      throw new Error('Transaction not found.');
    }

    const entries = await tx
      .select()
      .from(ledgerEntries)
      .where(eq(ledgerEntries.transactionId, transactionId));

    for (const entry of entries) {
      // CREDIT reduced balance -> reverse by adding; DEBIT increased balance -> reverse by subtracting
      if (entry.entryType === 'CREDIT') {
        await tx
          .update(accounts)
          .set({ balance: sql`${accounts.balance} + ${entry.amount}::numeric` })
          .where(and(eq(accounts.id, entry.accountId), eq(accounts.userId, userId)));
      } else {
        await tx
          .update(accounts)
          .set({ balance: sql`GREATEST(0, ${accounts.balance} - ${entry.amount}::numeric)` })
          .where(and(eq(accounts.id, entry.accountId), eq(accounts.userId, userId)));
      }
    }

    await tx.delete(transactions).where(and(eq(transactions.id, transactionId), eq(transactions.userId, userId)));
    return { deletedId: transactionId };
  });
}

/**
 * Pays a bill (full, partial, or split across accounts) atomically.
 */
export async function payBillAtomic(params: {
  userId: string;
  billId: string;
  splits: Array<{ accountId: string; amount: number }>;
  fee?: number;
  receiptUrl?: string;
  notes?: string;
  idempotencyKey?: string;
}) {
  const fee = Math.max(0, Number(params.fee ?? 0));
  if (params.splits.length === 0) throw new Error('Select at least one payment account.');
  const totalPay = params.splits.reduce((s, x) => s + x.amount, 0);
  if (totalPay <= 0) throw new Error('Payment amount must be greater than zero.');

  const poolDb = getPoolDb();
  return poolDb.transaction(async (tx) => {
    const [bill] = await tx
      .select()
      .from(bills)
      .where(and(eq(bills.id, params.billId), eq(bills.userId, params.userId)));
    if (!bill) throw new Error('Bill not found.');

    const accountNames: string[] = [];
    for (let i = 0; i < params.splits.length; i++) {
      const sp = params.splits[i];
      const debit = sp.amount + (i === 0 ? fee : 0);
      const [acc] = await tx
        .select()
        .from(accounts)
        .where(and(eq(accounts.id, sp.accountId), eq(accounts.userId, params.userId)));
      if (!acc) throw new Error('Selected payment account not found.');
      if (Number(acc.balance) < debit) {
        throw new Error(`Insufficient balance in ${acc.name}.`);
      }
      accountNames.push(acc.name);

      await tx
        .update(accounts)
        .set({ balance: sql`${accounts.balance} - ${debit.toFixed(2)}::numeric` })
        .where(and(eq(accounts.id, acc.id), eq(accounts.userId, params.userId)));

      const [createdTx] = await tx
        .insert(transactions)
        .values({
          userId: params.userId,
          type: 'EXPENSE',
          amount: debit.toFixed(2),
          fee: (i === 0 ? fee : 0).toFixed(2),
          currency: 'KES',
          date: new Date(),
          description: `${bill.title} Bill`,
          merchant: bill.title,
          reference: bill.accountNumber || null,
          receiptUrl: params.receiptUrl || null,
          sourceAccountId: acc.id,
        })
        .returning();

      await tx.insert(ledgerEntries).values({
        transactionId: createdTx.id,
        accountId: acc.id,
        entryType: 'CREDIT',
        amount: debit.toFixed(2),
      });

      await tx.insert(transactionSplits).values({
        transactionId: createdTx.id,
        category: bill.category || 'Bills & Utilities',
        amount: debit.toFixed(2),
        isBusiness: false,
        notes: params.notes || `Bill payment for ${bill.title}`,
      });
    }

    const newPaid = Number(bill.paidAmount) + totalPay;
    const isNowPaid = newPaid >= Number(bill.amount) - 0.01;

    const [updatedBill] = await tx
      .update(bills)
      .set({
        paidAmount: (isNowPaid ? Number(bill.amount) : newPaid).toFixed(2),
        isPaid: isNowPaid,
      })
      .where(and(eq(bills.id, bill.id), eq(bills.userId, params.userId)))
      .returning();

    const [paymentRecord] = await tx
      .insert(billPayments)
      .values({
        userId: params.userId,
        billId: bill.id,
        billTitle: bill.title,
        amountPaid: totalPay.toFixed(2),
        fee: fee.toFixed(2),
        accountName: accountNames.join(' + '),
        receiptUrl: params.receiptUrl || null,
        notes: params.notes || null,
      })
      .returning();

    return { bill: updatedBill, payment: paymentRecord };
  });
}

export async function getDashboardOverview(userId: string) {
  const db = getDb();

  const [
    userRow,
    userAccounts,
    recentTx,
    userBudgets,
    userBills,
    userBillPayments,
    userGoals,
    userDebts,
    userHoldings,
    userAssets,
    userLiabilities,
    userNotifications,
    userCats,
    userSchedules,
  ] = await Promise.all([
    db.query.users.findFirst({ where: eq(users.id, userId) }),
    db.select().from(accounts).where(and(eq(accounts.userId, userId), eq(accounts.isArchived, false))),
    db.select().from(transactions).where(eq(transactions.userId, userId)).orderBy(desc(transactions.date)).limit(50),
    db.select().from(budgets).where(eq(budgets.userId, userId)),
    db.select().from(bills).where(eq(bills.userId, userId)),
    db.select().from(billPayments).where(eq(billPayments.userId, userId)).orderBy(desc(billPayments.paidAt)).limit(30),
    db.select().from(savingsGoals).where(eq(savingsGoals.userId, userId)),
    db.select().from(debts).where(eq(debts.userId, userId)),
    db.select().from(holdings).where(eq(holdings.userId, userId)),
    db.select().from(assets).where(eq(assets.userId, userId)),
    db.select().from(liabilities).where(eq(liabilities.userId, userId)),
    db.select().from(notifications).where(eq(notifications.userId, userId)).orderBy(desc(notifications.createdAt)).limit(20),
    db.select().from(userCategories).where(eq(userCategories.userId, userId)),
    db.select().from(recurringSchedules).where(eq(recurringSchedules.userId, userId)),
  ]);

  const txIds = recentTx.map((t) => t.id);
  const splitsRows =
    txIds.length > 0
      ? await db.select().from(transactionSplits).where(inArray(transactionSplits.transactionId, txIds))
      : [];

  const splitsByTx = new Map<string, typeof splitsRows>();
  for (const sp of splitsRows) {
    const arr = splitsByTx.get(sp.transactionId) || [];
    arr.push(sp);
    splitsByTx.set(sp.transactionId, arr);
  }

  const totalAccountBalances = userAccounts
    .filter((a) => a.type !== 'INVESTMENT_CASH')
    .reduce((sum, a) => sum + Number(a.balance), 0);

  const unpaidBills = userBills.filter((b) => !b.isPaid);
  const reservedAmount = unpaidBills.reduce(
    (sum, b) => sum + Math.max(0, Number(b.amount) - Number(b.paidAmount || 0)),
    0
  );
  const availableCash = Math.max(0, totalAccountBalances - reservedAmount);

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

  // Calculate per-budget category spent for the current month
  const monthExpenseIds = monthTx.filter((t) => t.type === 'EXPENSE').map((t) => t.id);
  const monthSplits =
    monthExpenseIds.length > 0
      ? await db.select().from(transactionSplits).where(inArray(transactionSplits.transactionId, monthExpenseIds))
      : [];

  const spentByCategory = new Map<string, number>();
  for (const sp of monthSplits) {
    const key = sp.category.toLowerCase();
    spentByCategory.set(key, (spentByCategory.get(key) || 0) + Number(sp.amount));
  }

  const enrichedBudgets = userBudgets.map((b) => {
    const spent = spentByCategory.get(b.category.toLowerCase()) || 0;
    const limit = Number(b.limitAmount);
    return {
      ...b,
      spentAmount: spent,
      percentage: limit > 0 ? Math.round((spent / limit) * 100) : 0,
    };
  });

  const monthlyBudgetTarget = userBudgets.reduce((sum, b) => sum + Number(b.limitAmount), 0);
  const totalPortfolioValuation = userHoldings.reduce(
    (sum, h) => sum + Number(h.units) * Number(h.currentPrice) * Number(h.fxRateToPrimary),
    0
  );
  const totalInvestedCapital = userHoldings.reduce(
    (sum, h) => sum + Number(h.units) * Number(h.purchasePrice) * Number(h.fxRateToPrimary),
    0
  );
  const totalNonAccountAssets = userAssets.reduce((sum, a) => sum + Number(a.currentValue), 0);
  const totalDebt = userDebts.reduce((sum, d) => sum + Number(d.currentBalance), 0);
  const totalOtherLiabilities = userLiabilities.reduce((sum, l) => sum + Number(l.currentBalance), 0);
  const totalLiabilities = totalDebt + totalOtherLiabilities;
  const totalAssets = totalAccountBalances + totalPortfolioValuation + totalNonAccountAssets;
  const totalNetWorth = totalAssets - totalLiabilities;

  return {
    user: userRow,
    currency: 'KES',
    totalAccountBalances,
    reservedAmount,
    availableCash,
    monthlyIncome,
    monthlySpent,
    netCashFlow: monthlyIncome - monthlySpent,
    monthlyBudgetTarget,
    totalRemaining: Math.max(0, monthlyBudgetTarget - monthlySpent),
    budgetConsumptionPercent:
      monthlyBudgetTarget > 0 ? Math.round((monthlySpent / monthlyBudgetTarget) * 100) : 0,
    totalPortfolioValuation,
    totalInvestedCapital,
    totalPortfolioReturn: totalPortfolioValuation - totalInvestedCapital,
    totalNonAccountAssets,
    totalAssets,
    totalDebt,
    totalOtherLiabilities,
    totalLiabilities,
    totalNetWorth,
    accounts: userAccounts,
    transactions: recentTx.map((t) => ({
      ...t,
      splits: splitsByTx.get(t.id) || [],
    })),
    budgets: enrichedBudgets,
    bills: userBills,
    billPayments: userBillPayments,
    goals: userGoals,
    debts: userDebts,
    holdings: userHoldings,
    assets: userAssets,
    liabilities: userLiabilities,
    notifications: userNotifications,
    categories: userCats,
    recurringSchedules: userSchedules,
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
