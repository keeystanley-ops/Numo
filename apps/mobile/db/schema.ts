import { pgTable, text, timestamp, numeric, boolean, uuid, pgEnum, integer } from 'drizzle-orm/pg-core';
import { relations } from 'drizzle-orm';

// Enums
export const accountTypeEnum = pgEnum('account_type', [
  'CASH',
  'MPESA',
  'BANK',
  'SAVINGS',
  'INVESTMENT_CASH'
]);

export const entryTypeEnum = pgEnum('entry_type', ['DEBIT', 'CREDIT']);

export const transactionTypeEnum = pgEnum('transaction_type', [
  'EXPENSE',
  'INCOME',
  'TRANSFER',
  'INVESTMENT_CONTRIBUTION',
  'DEBT_PAYMENT'
]);

export const assetClassEnum = pgEnum('asset_class', [
  'STOCK_NSE',
  'STOCK_GLOBAL',
  'MMF',
  'TBILL',
  'BOND',
  'SACCO',
  'REIT',
  'CRYPTO',
  'REAL_ESTATE',
  'CUSTOM'
]);

export const activityTypeEnum = pgEnum('activity_type', [
  'BUY',
  'SELL',
  'DEPOSIT',
  'WITHDRAWAL',
  'DIVIDEND',
  'INTEREST',
  'SPLIT',
  'BONUS',
  'VALUATION_UPDATE'
]);

// 1. Users (Synced from Clerk)
export const users = pgTable('users', {
  id: text('id').primaryKey(), // Clerk User ID
  email: text('email').notNull(),
  primaryCurrency: text('primary_currency').default('KES').notNull(),
  country: text('country').default('KE').notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  updatedAt: timestamp('updated_at').defaultNow().notNull(),
});

// 2. Accounts (Cash, M-PESA, Banks, Savings)
export const accounts = pgTable('accounts', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id').references(() => users.id, { onDelete: 'cascade' }).notNull(),
  name: text('name').notNull(),
  type: accountTypeEnum('type').notNull(),
  currency: text('currency').default('KES').notNull(),
  balance: numeric('balance', { precision: 18, scale: 2 }).default('0.00').notNull(),
  isArchived: boolean('is_archived').default(false).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 3. Transactions (High-Level Financial Event)
export const transactions = pgTable('transactions', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id').references(() => users.id, { onDelete: 'cascade' }).notNull(),
  type: transactionTypeEnum('type').notNull(),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  currency: text('currency').default('KES').notNull(),
  date: timestamp('date').notNull(),
  description: text('description').notNull(),
  merchant: text('merchant'),
  receiptUrl: text('receipt_url'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 4. Ledger Entries (Strict Double-Entry Bookkeeping)
export const ledgerEntries = pgTable('ledger_entries', {
  id: uuid('id').defaultRandom().primaryKey(),
  transactionId: uuid('transaction_id').references(() => transactions.id, { onDelete: 'cascade' }).notNull(),
  accountId: uuid('account_id').references(() => accounts.id, { onDelete: 'cascade' }).notNull(),
  entryType: entryTypeEnum('entry_type').notNull(), // DEBIT or CREDIT
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 5. Transaction Splits (Multi-category with Personal vs. Business Tagging)
export const transactionSplits = pgTable('transaction_splits', {
  id: uuid('id').defaultRandom().primaryKey(),
  transactionId: uuid('transaction_id').references(() => transactions.id, { onDelete: 'cascade' }).notNull(),
  category: text('category').notNull(),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  isBusiness: boolean('is_business').default(false).notNull(),
  notes: text('notes'),
});

// 6. Budgets
export const budgets = pgTable('budgets', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id').references(() => users.id, { onDelete: 'cascade' }).notNull(),
  category: text('category').notNull(),
  limitAmount: numeric('limit_amount', { precision: 18, scale: 2 }).notNull(),
  period: text('period').default('MONTHLY').notNull(),
  alertThreshold: integer('alert_threshold').default(80).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 7. Debts (Liabilities for Avalanche & Snowball)
export const debts = pgTable('debts', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id').references(() => users.id, { onDelete: 'cascade' }).notNull(),
  name: text('name').notNull(),
  originalAmount: numeric('original_amount', { precision: 18, scale: 2 }).notNull(),
  currentBalance: numeric('current_balance', { precision: 18, scale: 2 }).notNull(),
  interestRate: numeric('interest_rate', { precision: 5, scale: 2 }).notNull(),
  monthlyPayment: numeric('monthly_payment', { precision: 18, scale: 2 }).notNull(),
  dueDate: integer('due_date').notNull(),
  lender: text('lender'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 8. Holdings (Multi-Asset Investments)
export const holdings = pgTable('holdings', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id').references(() => users.id, { onDelete: 'cascade' }).notNull(),
  assetClass: assetClassEnum('asset_class').notNull(),
  name: text('name').notNull(),
  ticker: text('ticker'),
  units: numeric('units', { precision: 18, scale: 4 }).notNull(),
  purchasePrice: numeric('purchase_price', { precision: 18, scale: 2 }).notNull(),
  currentPrice: numeric('current_price', { precision: 18, scale: 2 }).notNull(),
  currency: text('currency').default('KES').notNull(),
  dividendYield: numeric('dividend_yield', { precision: 5, scale: 2 }), // Projected annual %
  dividendFrequency: text('dividend_frequency').default('ANNUAL'), // ANNUAL, SEMI_ANNUAL, QUARTERLY, MONTHLY
  lastValuationDate: timestamp('last_valuation_date').defaultNow().notNull(),
});

// 9. Valuation Snapshots (Append-Only Historical Valuation Log)
export const valuationSnapshots = pgTable('valuation_snapshots', {
  id: uuid('id').defaultRandom().primaryKey(),
  holdingId: uuid('holding_id').references(() => holdings.id, { onDelete: 'cascade' }).notNull(),
  valuation: numeric('valuation', { precision: 18, scale: 2 }).notNull(),
  notes: text('notes'),
  timestamp: timestamp('timestamp').defaultNow().notNull(),
});

// 10. Investment Activities (Buys, Sells, Dividends, Withholding Taxes)
export const investmentActivities = pgTable('investment_activities', {
  id: uuid('id').defaultRandom().primaryKey(),
  holdingId: uuid('holding_id').references(() => holdings.id, { onDelete: 'cascade' }).notNull(),
  type: activityTypeEnum('type').notNull(),
  units: numeric('units', { precision: 18, scale: 4 }).default('0'),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  fees: numeric('fees', { precision: 18, scale: 2 }).default('0.00'),
  withholdingTax: numeric('withholding_tax', { precision: 18, scale: 2 }).default('0.00'),
  date: timestamp('date').notNull(),
  notes: text('notes'),
});

// Relationships
export const usersRelations = relations(users, ({ many }) => ({
  accounts: many(accounts),
  transactions: many(transactions),
  budgets: many(budgets),
  debts: many(debts),
  holdings: many(holdings),
}));

export const transactionsRelations = relations(transactions, ({ one, many }) => ({
  user: one(users, { fields: [transactions.userId], references: [users.id] }),
  ledgerEntries: many(ledgerEntries),
  splits: many(transactionSplits),
}));

export const holdingsRelations = relations(holdings, ({ one, many }) => ({
  user: one(users, { fields: [holdings.userId], references: [users.id] }),
  valuations: many(valuationSnapshots),
  activities: many(investmentActivities),
}));
