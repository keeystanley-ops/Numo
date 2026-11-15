import fs from 'node:fs';
import path from 'node:path';
import { eq } from 'drizzle-orm';
import {
  accounts,
  bills,
  budgets,
  debts,
  getDb,
  holdings,
  investmentActivities,
  ledgerEntries,
  savingsGoals,
  transactions,
  transactionSplits,
  users,
  valuationSnapshots,
} from './index';

function loadEnvLocal() {
  const envPath = path.join(process.cwd(), '.env.local');
  if (!fs.existsSync(envPath)) return;
  for (const raw of fs.readFileSync(envPath, 'utf8').split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith('#')) continue;
    const eqIdx = line.indexOf('=');
    if (eqIdx <= 0) continue;
    const key = line.slice(0, eqIdx).trim();
    const val = line.slice(eqIdx + 1).trim();
    if (!process.env[key]) process.env[key] = val;
  }
}

async function seed() {
  loadEnvLocal();
  const db = getDb();
  const userId = 'demo_user_ayesha';

  console.log('Seeding Numo Neon PostgreSQL database...');

  // 1. User
  await db
    .insert(users)
    .values({
      id: userId,
      email: 'ayesha@numo.com',
      fullName: 'Ayesha Wanjiru',
      primaryCurrency: 'KES',
      country: 'KE',
      onboardingCompleted: true,
      preferredEnvironment: 'MONEY',
    })
    .onConflictDoNothing();

  // Skip re-seeding if accounts already exist for demo user
  const existingAccounts = await db.select().from(accounts).where(eq(accounts.userId, userId));
  if (existingAccounts.length > 0) {
    console.log('Seed data already exists for demo_user_ayesha. Skipping duplicate insert.');
    return;
  }

  // 2. Accounts
  const insertedAccounts = await db
    .insert(accounts)
    .values([
      { userId, name: 'M-PESA Wallet', type: 'MPESA', currency: 'KES', balance: '18500.00' },
      { userId, name: 'Equity Bank', type: 'BANK', currency: 'KES', balance: '42000.00' },
      { userId, name: 'Cash on Hand', type: 'CASH', currency: 'KES', balance: '8000.00' },
      { userId, name: 'Sanlam MMF Cash', type: 'INVESTMENT_CASH', currency: 'KES', balance: '25000.00' },
    ])
    .returning();

  const mpesa = insertedAccounts.find((a) => a.type === 'MPESA')!;
  const bank = insertedAccounts.find((a) => a.type === 'BANK')!;

  // 3. Budgets
  await db.insert(budgets).values([
    { userId, category: 'Food & Dining', limitAmount: '30000.00', period: 'MONTHLY', alertThreshold: 80 },
    { userId, category: 'Transport', limitAmount: '20000.00', period: 'MONTHLY', alertThreshold: 80 },
    { userId, category: 'Shopping', limitAmount: '25000.00', period: 'MONTHLY', alertThreshold: 80 },
    { userId, category: 'Health & Fitness', limitAmount: '10000.00', period: 'MONTHLY', alertThreshold: 80 },
    { userId, category: 'Entertainment', limitAmount: '8000.00', period: 'MONTHLY', alertThreshold: 80 },
    { userId, category: 'Bills & Utilities', limitAmount: '15000.00', period: 'MONTHLY', alertThreshold: 80 },
    { userId, category: 'Other', limitAmount: '12000.00', period: 'MONTHLY', alertThreshold: 80 },
  ]);

  // 4. Transactions + Double-Entry Ledger + Splits
  const now = Date.now();
  const sampleTx = [
    {
      type: 'INCOME' as const,
      amount: '120000.00',
      description: 'Monthly Salary',
      merchant: 'Employer Payroll',
      accountId: bank.id,
      entryType: 'DEBIT' as const,
      category: 'Income',
      date: new Date(now - 86400_000 * 3),
    },
    {
      type: 'EXPENSE' as const,
      amount: '1250.00',
      description: 'Groceries',
      merchant: 'Naivas Supermarket',
      accountId: mpesa.id,
      entryType: 'CREDIT' as const,
      category: 'Food & Dining',
      date: new Date(now - 3600_000 * 5),
    },
    {
      type: 'EXPENSE' as const,
      amount: '320.00',
      description: 'Uber Ride',
      merchant: 'Uber Kenya',
      accountId: mpesa.id,
      entryType: 'CREDIT' as const,
      category: 'Transport',
      date: new Date(now - 3600_000 * 2),
    },
    {
      type: 'EXPENSE' as const,
      amount: '3200.00',
      description: 'Electricity Bill',
      merchant: 'KPLC Tokens',
      accountId: mpesa.id,
      entryType: 'CREDIT' as const,
      category: 'Bills & Utilities',
      date: new Date(now - 86400_000),
    },
  ];

  for (const item of sampleTx) {
    const [txRow] = await db
      .insert(transactions)
      .values({
        userId,
        type: item.type,
        amount: item.amount,
        currency: 'KES',
        date: item.date,
        description: item.description,
        merchant: item.merchant,
      })
      .returning();

    await db.insert(ledgerEntries).values({
      transactionId: txRow.id,
      accountId: item.accountId,
      entryType: item.entryType,
      amount: item.amount,
    });

    await db.insert(transactionSplits).values({
      transactionId: txRow.id,
      category: item.category,
      amount: item.amount,
      isBusiness: false,
    });
  }

  // 5. Upcoming Bills
  await db.insert(bills).values([
    {
      userId,
      title: 'Rent Payment',
      amount: '35000.00',
      dueDate: new Date(now + 86400_000 * 2),
      category: 'Rent',
      accountNumber: '99881122',
      isPaid: false,
    },
    {
      userId,
      title: 'KPLC Electricity',
      amount: '2500.00',
      dueDate: new Date(now + 86400_000 * 4),
      category: 'Bills & Utilities',
      accountNumber: '12345678',
      isPaid: false,
    },
    {
      userId,
      title: 'Safaricom Home Fibre',
      amount: '2999.00',
      dueDate: new Date(now + 86400_000 * 5),
      category: 'Bills & Utilities',
      accountNumber: '44556677',
      isPaid: false,
    },
  ]);

  // 6. Savings Goals
  await db.insert(savingsGoals).values([
    {
      userId,
      title: 'Emergency Fund',
      targetAmount: '250000.00',
      savedAmount: '180000.00',
      monthlyContribution: '10000.00',
      runwayMonths: 6,
      iconType: 'Shield',
    },
    {
      userId,
      title: 'Dream Vacation',
      targetAmount: '200000.00',
      savedAmount: '25000.00',
      monthlyContribution: '5000.00',
      iconType: 'Flight',
    },
    {
      userId,
      title: 'New Laptop',
      targetAmount: '100000.00',
      savedAmount: '60000.00',
      monthlyContribution: '15000.00',
      iconType: 'Laptop',
    },
  ]);

  // 7. Debts
  await db.insert(debts).values([
    {
      userId,
      name: 'HELB Student Loan',
      originalAmount: '220000.00',
      currentBalance: '145000.00',
      interestRate: '4.00',
      monthlyPayment: '3500.00',
      dueDate: 25,
      lender: 'HELB',
    },
    {
      userId,
      name: 'KCB Personal Loan',
      originalAmount: '120000.00',
      currentBalance: '85000.00',
      interestRate: '13.50',
      monthlyPayment: '6200.00',
      dueDate: 28,
      lender: 'KCB Bank',
    },
  ]);

  // 8. Investment Holdings + Snapshots + Activities
  const insertedHoldings = await db
    .insert(holdings)
    .values([
      {
        userId,
        assetClass: 'STOCK_NSE',
        name: 'Safaricom PLC (NSE: SCOM)',
        ticker: 'SCOM',
        units: '10000.0000',
        purchasePrice: '14.80',
        currentPrice: '17.50',
        currency: 'KES',
        dividendYield: '6.20',
        dividendFrequency: 'SEMI_ANNUAL',
      },
      {
        userId,
        assetClass: 'MMF',
        name: 'Sanlam Money Market Fund',
        ticker: 'SANLAM-MMF',
        units: '1800.0000',
        purchasePrice: '88.89',
        currentPrice: '100.00',
        currency: 'KES',
        dividendYield: '12.80',
        dividendFrequency: 'MONTHLY',
      },
      {
        userId,
        assetClass: 'SACCO',
        name: 'Stima SACCO Member Shares',
        ticker: 'STIMA',
        units: '800.0000',
        purchasePrice: '87.50',
        currentPrice: '100.00',
        currency: 'KES',
        dividendYield: '10.50',
        dividendFrequency: 'ANNUAL',
      },
      {
        userId,
        assetClass: 'TBILL',
        name: '91-Day Kenya Treasury Bill',
        ticker: 'TBILL-91',
        units: '500.0000',
        purchasePrice: '96.00',
        currentPrice: '100.00',
        currency: 'KES',
        dividendYield: '15.20',
        dividendFrequency: 'QUARTERLY',
      },
    ])
    .returning();

  for (const h of insertedHoldings) {
    const val = (Number(h.units) * Number(h.currentPrice)).toFixed(2);
    await db.insert(valuationSnapshots).values({
      holdingId: h.id,
      valuation: val,
      notes: 'Initial seed valuation',
    });
    await db.insert(investmentActivities).values({
      holdingId: h.id,
      type: 'BUY',
      units: h.units,
      amount: (Number(h.units) * Number(h.purchasePrice)).toFixed(2),
      date: new Date(now - 86400_000 * 30),
      notes: `Initial position in ${h.name}`,
    });
  }

  console.log('Seed completed successfully!');
}

seed()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error('Seed failed:', err);
    process.exit(1);
  });
