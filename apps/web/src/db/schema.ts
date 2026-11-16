import {
  pgTable,
  text,
  timestamp,
  numeric,
  boolean,
  uuid,
  pgEnum,
  integer,
  jsonb,
} from 'drizzle-orm/pg-core';
import { relations } from 'drizzle-orm';

// Enums
export const accountTypeEnum = pgEnum('account_type', [
  'CASH',
  'MPESA',
  'BANK',
  'SAVINGS',
  'INVESTMENT_CASH',
]);

export const entryTypeEnum = pgEnum('entry_type', ['DEBIT', 'CREDIT']);

export const transactionTypeEnum = pgEnum('transaction_type', [
  'EXPENSE',
  'INCOME',
  'TRANSFER',
  'INVESTMENT_CONTRIBUTION',
  'DEBT_PAYMENT',
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
  'CUSTOM',
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
  'VALUATION_UPDATE',
]);

// 1. Users (Synced from Clerk)
export const users = pgTable('users', {
  id: text('id').primaryKey(), // Clerk User ID
  email: text('email').notNull(),
  fullName: text('full_name'),
  firstName: text('first_name'),
  phone: text('phone'),
  primaryCurrency: text('primary_currency').default('KES').notNull(),
  country: text('country').default('KE').notNull(),
  onboardingCompleted: boolean('onboarding_completed').default(false).notNull(),
  preferredEnvironment: text('preferred_environment').default('MONEY').notNull(),
  biometricEnabled: boolean('biometric_enabled').default(false).notNull(),
  privacyHideBalances: boolean('privacy_hide_balances').default(false).notNull(),
  voiceSpeed: text('voice_speed').default('Normal').notNull(),
  voiceName: text('voice_name').default('Sulafat').notNull(),
  language: text('language').default('English (Kenya)').notNull(),
  reminderNotifications: boolean('reminder_notifications').default(true).notNull(),
  budgetAlertNotifications: boolean('budget_alert_notifications').default(true).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  updatedAt: timestamp('updated_at').defaultNow().notNull(),
});

// 2. Accounts (Cash, M-PESA, Banks, Savings, Investment Cash)
export const accounts = pgTable('accounts', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  name: text('name').notNull(),
  provider: text('provider').default('Numo').notNull(),
  accountNumber: text('account_number'),
  type: accountTypeEnum('type').notNull(),
  currency: text('currency').default('KES').notNull(),
  balance: numeric('balance', { precision: 18, scale: 2 }).default('0.00').notNull(),
  isDefaultCash: boolean('is_default_cash').default(false).notNull(),
  isArchived: boolean('is_archived').default(false).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 3. Transactions (High-Level Financial Event)
export const transactions = pgTable('transactions', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  type: transactionTypeEnum('type').notNull(),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  grossAmount: numeric('gross_amount', { precision: 18, scale: 2 }),
  deductionAmount: numeric('deduction_amount', { precision: 18, scale: 2 }).default('0.00'),
  fee: numeric('fee', { precision: 18, scale: 2 }).default('0.00'),
  currency: text('currency').default('KES').notNull(),
  date: timestamp('date').notNull(),
  description: text('description').notNull(),
  merchant: text('merchant'),
  reference: text('reference'),
  receiptUrl: text('receipt_url'),
  sourceAccountId: uuid('source_account_id'),
  destinationAccountId: uuid('destination_account_id'),
  idempotencyKey: text('idempotency_key'),
  metadata: jsonb('metadata'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 4. Ledger Entries (Strict Double-Entry Bookkeeping)
export const ledgerEntries = pgTable('ledger_entries', {
  id: uuid('id').defaultRandom().primaryKey(),
  transactionId: uuid('transaction_id')
    .references(() => transactions.id, { onDelete: 'cascade' })
    .notNull(),
  accountId: uuid('account_id')
    .references(() => accounts.id, { onDelete: 'cascade' })
    .notNull(),
  entryType: entryTypeEnum('entry_type').notNull(), // DEBIT or CREDIT
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 5. Transaction Splits (Multi-category with Personal vs. Business Tagging)
export const transactionSplits = pgTable('transaction_splits', {
  id: uuid('id').defaultRandom().primaryKey(),
  transactionId: uuid('transaction_id')
    .references(() => transactions.id, { onDelete: 'cascade' })
    .notNull(),
  category: text('category').notNull(),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  isBusiness: boolean('is_business').default(false).notNull(),
  notes: text('notes'),
});

// 6. Budgets
export const budgets = pgTable('budgets', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  category: text('category').notNull(),
  limitAmount: numeric('limit_amount', { precision: 18, scale: 2 }).notNull(),
  period: text('period').default('MONTHLY').notNull(),
  alertThreshold: integer('alert_threshold').default(80).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 7. Upcoming Bills & Obligations
export const bills = pgTable('bills', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  title: text('title').notNull(),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  paidAmount: numeric('paid_amount', { precision: 18, scale: 2 }).default('0.00').notNull(),
  dueDate: timestamp('due_date').notNull(),
  category: text('category').default('Bills & Utilities').notNull(),
  accountNumber: text('account_number'),
  recurrence: text('recurrence').default('MONTHLY').notNull(),
  reminderEnabled: boolean('reminder_enabled').default(true).notNull(),
  reminderLeadDays: integer('reminder_lead_days').default(3).notNull(),
  defaultAccountId: uuid('default_account_id'),
  autoPayPreference: boolean('auto_pay_preference').default(false).notNull(),
  isPaid: boolean('is_paid').default(false).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 7b. Bill Payment History (Preserved even if bill is deleted)
export const billPayments = pgTable('bill_payments', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  billId: uuid('bill_id'),
  billTitle: text('bill_title').notNull(),
  amountPaid: numeric('amount_paid', { precision: 18, scale: 2 }).notNull(),
  fee: numeric('fee', { precision: 18, scale: 2 }).default('0.00').notNull(),
  accountName: text('account_name').notNull(),
  receiptUrl: text('receipt_url'),
  notes: text('notes'),
  paidAt: timestamp('paid_at').defaultNow().notNull(),
});

// 8. Savings Goals & Emergency Runway
export const savingsGoals = pgTable('savings_goals', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  title: text('title').notNull(),
  targetAmount: numeric('target_amount', { precision: 18, scale: 2 }).notNull(),
  savedAmount: numeric('saved_amount', { precision: 18, scale: 2 }).default('0.00').notNull(),
  monthlyContribution: numeric('monthly_contribution', { precision: 18, scale: 2 })
    .default('0.00')
    .notNull(),
  runwayMonths: integer('runway_months'),
  iconType: text('icon_type').default('Shield').notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 9. Debts (Liabilities for Avalanche & Snowball)
export const debts = pgTable('debts', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  name: text('name').notNull(),
  originalAmount: numeric('original_amount', { precision: 18, scale: 2 }).notNull(),
  currentBalance: numeric('current_balance', { precision: 18, scale: 2 }).notNull(),
  interestRate: numeric('interest_rate', { precision: 5, scale: 2 }).notNull(),
  monthlyPayment: numeric('monthly_payment', { precision: 18, scale: 2 }).notNull(),
  dueDate: integer('due_date').notNull(),
  lender: text('lender'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 10. Holdings (Multi-Asset Investments)
export const holdings = pgTable('holdings', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  assetClass: assetClassEnum('asset_class').notNull(),
  name: text('name').notNull(),
  ticker: text('ticker'),
  units: numeric('units', { precision: 18, scale: 4 }).notNull(),
  purchasePrice: numeric('purchase_price', { precision: 18, scale: 2 }).notNull(),
  currentPrice: numeric('current_price', { precision: 18, scale: 2 }).notNull(),
  currency: text('currency').default('KES').notNull(),
  fxRateToPrimary: numeric('fx_rate_to_primary', { precision: 12, scale: 4 })
    .default('1.0000')
    .notNull(),
  dividendYield: numeric('dividend_yield', { precision: 5, scale: 2 }),
  dividendFrequency: text('dividend_frequency').default('ANNUAL'),
  lastValuationDate: timestamp('last_valuation_date').defaultNow().notNull(),
});

// 11. Valuation Snapshots (Append-Only Historical Valuation Log)
export const valuationSnapshots = pgTable('valuation_snapshots', {
  id: uuid('id').defaultRandom().primaryKey(),
  holdingId: uuid('holding_id')
    .references(() => holdings.id, { onDelete: 'cascade' })
    .notNull(),
  valuation: numeric('valuation', { precision: 18, scale: 2 }).notNull(),
  notes: text('notes'),
  timestamp: timestamp('timestamp').defaultNow().notNull(),
});

// 12. Investment Activities (Buys, Sells, Dividends, Withholding Taxes)
export const investmentActivities = pgTable('investment_activities', {
  id: uuid('id').defaultRandom().primaryKey(),
  holdingId: uuid('holding_id')
    .references(() => holdings.id, { onDelete: 'cascade' })
    .notNull(),
  type: activityTypeEnum('type').notNull(),
  units: numeric('units', { precision: 18, scale: 4 }).default('0'),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  fees: numeric('fees', { precision: 18, scale: 2 }).default('0.00'),
  withholdingTax: numeric('withholding_tax', { precision: 18, scale: 2 }).default('0.00'),
  date: timestamp('date').notNull(),
  notes: text('notes'),
});

// 13. Hey Numo Assistant Sessions & Multi-Turn Memory
export const assistantSessions = pgTable('assistant_sessions', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  title: text('title').default('Hey Numo Chat').notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  updatedAt: timestamp('updated_at').defaultNow().notNull(),
});

export const assistantMessages = pgTable('assistant_messages', {
  id: uuid('id').defaultRandom().primaryKey(),
  sessionId: uuid('session_id')
    .references(() => assistantSessions.id, { onDelete: 'cascade' })
    .notNull(),
  role: text('role').notNull(), // 'user' | 'model'
  content: text('content').notNull(),
  pendingAction: jsonb('pending_action'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 14. Recurring Schedules (Income, Transfers, Bills)
export const recurringSchedules = pgTable('recurring_schedules', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  title: text('title').notNull(),
  type: transactionTypeEnum('type').notNull(),
  amount: numeric('amount', { precision: 18, scale: 2 }).notNull(),
  fee: numeric('fee', { precision: 18, scale: 2 }).default('0.00').notNull(),
  category: text('category').notNull(),
  sourceAccountId: uuid('source_account_id'),
  destinationAccountId: uuid('destination_account_id'),
  frequency: text('frequency').default('MONTHLY').notNull(),
  startDate: timestamp('start_date').defaultNow().notNull(),
  isActive: boolean('is_active').default(true).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 15. Non-Account Assets (Real Estate, Vehicles, Equipment, Business Equity)
export const assets = pgTable('assets', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  name: text('name').notNull(),
  category: text('category').default('Property').notNull(),
  currentValue: numeric('current_value', { precision: 18, scale: 2 }).notNull(),
  notes: text('notes'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  updatedAt: timestamp('updated_at').defaultNow().notNull(),
});

// 16. Liabilities (Non-debt or general liabilities linked with Net Worth)
export const liabilities = pgTable('liabilities', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  name: text('name').notNull(),
  category: text('category').default('Loan').notNull(),
  currentBalance: numeric('current_balance', { precision: 18, scale: 2 }).notNull(),
  interestRate: numeric('interest_rate', { precision: 5, scale: 2 }).default('0.00'),
  notes: text('notes'),
  createdAt: timestamp('created_at').defaultNow().notNull(),
  updatedAt: timestamp('updated_at').defaultNow().notNull(),
});

// 17. Net Worth History Snapshots
export const netWorthSnapshots = pgTable('net_worth_snapshots', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  totalAssets: numeric('total_assets', { precision: 18, scale: 2 }).notNull(),
  totalLiabilities: numeric('total_liabilities', { precision: 18, scale: 2 }).notNull(),
  netWorth: numeric('net_worth', { precision: 18, scale: 2 }).notNull(),
  recordedAt: timestamp('recorded_at').defaultNow().notNull(),
});

// 18. Notifications
export const notifications = pgTable('notifications', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  title: text('title').notNull(),
  message: text('message').notNull(),
  type: text('type').default('INFO').notNull(), // INFO | ALERT | BILL | BUDGET
  isRead: boolean('is_read').default(false).notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// 19. Custom Categories
export const userCategories = pgTable('user_categories', {
  id: uuid('id').defaultRandom().primaryKey(),
  userId: text('user_id')
    .references(() => users.id, { onDelete: 'cascade' })
    .notNull(),
  name: text('name').notNull(),
  type: text('type').default('EXPENSE').notNull(), // EXPENSE | INCOME
  iconName: text('icon_name').default('Category').notNull(),
  colorHex: text('color_hex').default('#14B8A6').notNull(),
  createdAt: timestamp('created_at').defaultNow().notNull(),
});

// Relationships
export const usersRelations = relations(users, ({ many }) => ({
  accounts: many(accounts),
  transactions: many(transactions),
  budgets: many(budgets),
  bills: many(bills),
  billPayments: many(billPayments),
  savingsGoals: many(savingsGoals),
  debts: many(debts),
  holdings: many(holdings),
  assets: many(assets),
  liabilities: many(liabilities),
  notifications: many(notifications),
  userCategories: many(userCategories),
  assistantSessions: many(assistantSessions),
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

export const assistantSessionsRelations = relations(assistantSessions, ({ one, many }) => ({
  user: one(users, { fields: [assistantSessions.userId], references: [users.id] }),
  messages: many(assistantMessages),
}));
