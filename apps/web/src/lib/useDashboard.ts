'use client';

import { useCallback, useEffect, useState } from 'react';

export interface DashboardPayload {
  user: {
    id: string;
    email: string;
    fullName: string | null;
    firstName: string | null;
    phone: string | null;
    primaryCurrency: string;
    onboardingCompleted: boolean;
    preferredEnvironment: string;
    biometricEnabled: boolean;
    privacyHideBalances: boolean;
    voiceSpeed: string;
    voiceName: string;
    language: string;
    reminderNotifications: boolean;
    budgetAlertNotifications: boolean;
  } | null;
  currency: string;
  totalAccountBalances: number;
  reservedAmount: number;
  availableCash: number;
  monthlyIncome: number;
  monthlySpent: number;
  netCashFlow: number;
  monthlyBudgetTarget: number;
  totalRemaining: number;
  budgetConsumptionPercent: number;
  totalPortfolioValuation: number;
  totalInvestedCapital: number;
  totalPortfolioReturn: number;
  totalNonAccountAssets: number;
  totalAssets: number;
  totalDebt: number;
  totalOtherLiabilities: number;
  totalLiabilities: number;
  totalNetWorth: number;
  accounts: Array<{
    id: string;
    name: string;
    provider: string;
    accountNumber: string | null;
    type: string;
    balance: string;
    isDefaultCash: boolean;
  }>;
  transactions: Array<{
    id: string;
    description: string;
    type: 'EXPENSE' | 'INCOME' | 'TRANSFER' | 'INVESTMENT_CONTRIBUTION' | 'DEBT_PAYMENT';
    amount: string;
    grossAmount: string | null;
    deductionAmount: string | null;
    fee: string | null;
    date: string;
    merchant: string | null;
    reference: string | null;
    receiptUrl: string | null;
    sourceAccountId: string | null;
    destinationAccountId: string | null;
    splits: Array<{ id: string; category: string; amount: string; isBusiness: boolean; notes: string | null }>;
  }>;
  budgets: Array<{
    id: string;
    category: string;
    limitAmount: string;
    spentAmount: number;
    percentage: number;
    alertThreshold: number;
  }>;
  bills: Array<{
    id: string;
    title: string;
    amount: string;
    paidAmount: string;
    dueDate: string;
    category: string;
    accountNumber: string | null;
    recurrence: string;
    reminderEnabled: boolean;
    reminderLeadDays: number;
    autoPayPreference: boolean;
    isPaid: boolean;
  }>;
  billPayments: Array<{
    id: string;
    billId: string | null;
    billTitle: string;
    amountPaid: string;
    fee: string;
    accountName: string;
    receiptUrl: string | null;
    paidAt: string;
  }>;
  goals: Array<{
    id: string;
    title: string;
    targetAmount: string;
    savedAmount: string;
    monthlyContribution: string;
    runwayMonths: number | null;
    iconType: string;
  }>;
  debts: Array<{
    id: string;
    name: string;
    originalAmount: string;
    currentBalance: string;
    interestRate: string;
    monthlyPayment: string;
    dueDate: number;
    lender: string | null;
  }>;
  holdings: Array<{
    id: string;
    name: string;
    assetClass: string;
    ticker: string | null;
    units: string;
    purchasePrice: string;
    currentPrice: string;
    dividendYield: string | null;
    lastValuationDate: string;
  }>;
  assets: Array<{ id: string; name: string; category: string; currentValue: string; notes: string | null }>;
  liabilities: Array<{ id: string; name: string; category: string; currentBalance: string; interestRate: string | null }>;
  notifications: Array<{ id: string; title: string; message: string; type: string; isRead: boolean; createdAt: string }>;
  categories: Array<{ id: string; name: string; type: string; colorHex: string }>;
}

export function useDashboardData() {
  const [data, setData] = useState<DashboardPayload | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setError(null);
      const res = await fetch('/api/dashboard', { cache: 'no-store' });
      const json = await res.json();
      if (!res.ok) throw new Error(json.error || 'Failed to load financial data');
      setData(json);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load dashboard');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return { data, loading, error, refresh };
}
