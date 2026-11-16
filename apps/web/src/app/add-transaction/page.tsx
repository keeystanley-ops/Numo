'use client';

import { Suspense, useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { ArrowRightLeft, Calculator, Paperclip, X } from 'lucide-react';
import { NumoCalculatorModal } from '@/components/NumoCalculatorModal';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

type Mode = 'expense' | 'income' | 'transfer';

const EXPENSE_CATS = ['Food & Dining', 'Transport', 'Shopping', 'Health & Fitness', 'Housing', 'Entertainment', 'Education', 'Other'];
const INCOME_CATS = ['Salary', 'Freelance', 'Investments', 'Business', 'Other'];

function AddTransactionContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { data, refresh } = useDashboardData();

  const paramTab = (searchParams.get('tab') || 'expense').toLowerCase();
  const [mode, setMode] = useState<Mode>(
    paramTab === 'income' || paramTab === 'transfer' ? paramTab : 'expense'
  );

  useEffect(() => {
    if (paramTab === 'income' || paramTab === 'transfer' || paramTab === 'expense') {
      setMode(paramTab);
    }
  }, [paramTab]);

  const accent =
    mode === 'income' ? '#1b8a5a' : mode === 'transfer' ? '#00796b' : '#d81b60';

  const accounts = data?.accounts || [];
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [description, setDescription] = useState('');
  const [receiptUrl, setReceiptUrl] = useState('');
  const [showCalc, setShowCalc] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Expense state
  const [expenseCategory, setExpenseCategory] = useState('Food & Dining');
  const [expenseAccountId, setExpenseAccountId] = useState('');

  // Income state
  const [incomeCategory, setIncomeCategory] = useState('Salary');
  const [incomeAccountId, setIncomeAccountId] = useState('');
  const [applyTax, setApplyTax] = useState(false);
  const [deductionIsPct, setDeductionIsPct] = useState(true);
  const [taxValue, setTaxValue] = useState('18');
  const [splitDeposit, setSplitDeposit] = useState(false);
  const [splitMap, setSplitMap] = useState<Record<string, string>>({});
  const [incomeRecurring, setIncomeRecurring] = useState('ONE_TIME');

  // Transfer state
  const [fromAccountId, setFromAccountId] = useState('');
  const [toAccountId, setToAccountId] = useState('');
  const [transferFee, setTransferFee] = useState('0');
  const [transferRef, setTransferRef] = useState('');
  const [transferRecurring, setTransferRecurring] = useState('ONE_TIME');
  const [showReview, setShowReview] = useState(false);

  useEffect(() => {
    if (accounts.length > 0) {
      if (!expenseAccountId) setExpenseAccountId(accounts[0].id);
      if (!incomeAccountId) setIncomeAccountId(accounts[0].id);
      if (!fromAccountId) setFromAccountId(accounts[0].id);
      if (!toAccountId) setToAccountId(accounts[1]?.id || accounts[0].id);
    }
  }, [accounts, expenseAccountId, incomeAccountId, fromAccountId, toAccountId]);

  const gross = Number(amount || 0);
  const deductionAmount = !applyTax
    ? 0
    : deductionIsPct
      ? Math.min(gross, gross * (Math.min(100, Math.max(0, Number(taxValue || 0))) / 100))
      : Math.min(gross, Math.max(0, Number(taxValue || 0)));
  const netIncome = Math.max(0, gross - deductionAmount);
  const feeNum = Math.max(0, Number(transferFee || 0));
  const totalSourceDebit = gross + feeNum;

  const switchMode = (next: Mode) => {
    setMode(next);
    setError(null);
    router.replace(`/add-transaction?tab=${next}`, { scroll: false });
  };

  const handleBack = () => {
    if (window.history.length > 1) router.back();
    else router.push('/dashboard');
  };

  const handleSubmit = async () => {
    if (submitting) return;
    setError(null);

    if (gross <= 0) return setError('Enter a valid amount greater than zero.');

    const idempotencyKey = `tx_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

    if (mode === 'expense') {
      if (!expenseAccountId) return setError('Select an account.');
      setSubmitting(true);
      try {
        const res = await fetch('/api/transactions', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            type: 'EXPENSE',
            amount: gross,
            date,
            description: description.trim() || expenseCategory,
            receiptUrl: receiptUrl.trim() || undefined,
            sourceAccountId: expenseAccountId,
            splits: [{ category: expenseCategory, amount: gross }],
            idempotencyKey,
          }),
        });
        const json = await res.json();
        if (!res.ok) throw new Error(json.error || 'Failed to save expense');
        await refresh();
        handleBack();
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to save expense');
      } finally {
        setSubmitting(false);
      }
      return;
    }

    if (mode === 'income') {
      if (netIncome <= 0) return setError('Net credited income must be greater than zero.');
      let depositSplits: Array<{ accountId: string; amount: number }> = [];
      if (!splitDeposit) {
        if (!incomeAccountId) return setError('Select a destination account.');
        depositSplits = [{ accountId: incomeAccountId, amount: netIncome }];
      } else {
        depositSplits = accounts
          .map((a) => ({ accountId: a.id, amount: Number(splitMap[a.id] || 0) }))
          .filter((s) => s.amount > 0);
        const sum = depositSplits.reduce((s, x) => s + x.amount, 0);
        if (Math.abs(sum - netIncome) > 1) {
          return setError(`Split deposits must sum to ${formatKes(netIncome)}.`);
        }
      }

      setSubmitting(true);
      try {
        const res = await fetch('/api/transactions', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            type: 'INCOME',
            amount: netIncome,
            grossAmount: gross,
            deductionAmount,
            category: incomeCategory,
            date,
            description: description.trim() || incomeCategory,
            receiptUrl: receiptUrl.trim() || undefined,
            depositSplits,
            recurringFrequency: incomeRecurring,
            idempotencyKey,
          }),
        });
        const json = await res.json();
        if (!res.ok) throw new Error(json.error || 'Failed to save income');
        await refresh();
        handleBack();
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to save income');
      } finally {
        setSubmitting(false);
      }
      return;
    }

    // Transfer validation -> Review modal
    if (!fromAccountId || !toAccountId) return setError('Select source and destination accounts.');
    if (fromAccountId === toAccountId) return setError('Source and destination accounts cannot be the same.');
    setShowReview(true);
  };

  const executeTransfer = async () => {
    if (submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      const res = await fetch('/api/transactions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'TRANSFER',
          amount: gross,
          fee: feeNum,
          date,
          description: transferRef.trim() || 'Account Transfer',
          reference: transferRef.trim() || undefined,
          sourceAccountId: fromAccountId,
          destinationAccountId: toAccountId,
          recurringFrequency: transferRecurring,
          idempotencyKey: `tr_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
        }),
      });
      const json = await res.json();
      if (!res.ok) throw new Error(json.error || 'Transfer failed');
      setShowReview(false);
      await refresh();
      handleBack();
    } catch (err) {
      setShowReview(false);
      setError(err instanceof Error ? err.message : 'Transfer failed');
    } finally {
      setSubmitting(false);
    }
  };

  const fromAcc = accounts.find((a) => a.id === fromAccountId);
  const toAcc = accounts.find((a) => a.id === toAccountId);

  return (
    <NumoShell
      title="Add Transaction"
      rightSlot={
        <button onClick={handleBack} style={{ border: 'none', background: 'transparent', fontWeight: 700, color: '#5c4636', cursor: 'pointer' }}>
          Cancel
        </button>
      }
    >
      {/* 3-Mode Segmented Control */}
      <div className="numo-card" style={{ padding: 4, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 4, marginBottom: 14 }}>
        {(['expense', 'income', 'transfer'] as const).map((m) => {
          const active = mode === m;
          const bg = !active ? 'transparent' : m === 'income' ? '#1b8a5a' : m === 'transfer' ? '#00796b' : '#d81b60';
          return (
            <button
              key={m}
              onClick={() => switchMode(m)}
              style={{
                padding: '10px 8px',
                borderRadius: 16,
                border: 'none',
                cursor: 'pointer',
                fontWeight: 800,
                fontSize: 13,
                background: bg,
                color: active ? '#fff' : '#2d1f14',
                textTransform: 'capitalize',
              }}
            >
              {m}
            </button>
          );
        })}
      </div>

      {/* Transfer From / To Cards */}
      {mode === 'transfer' && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 14 }}>
          <div className="numo-card" style={{ flex: 1, padding: 12 }}>
            <div style={{ fontSize: 11, color: '#5c4636', fontWeight: 700 }}>From Account</div>
            <select
              className="numo-input"
              style={{ marginTop: 4 }}
              value={fromAccountId}
              onChange={(e) => setFromAccountId(e.target.value)}
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.name} ({formatKes(Number(a.balance))})
                </option>
              ))}
            </select>
          </div>
          <button
            type="button"
            aria-label="Swap accounts"
            onClick={() => {
              const tmp = fromAccountId;
              setFromAccountId(toAccountId);
              setToAccountId(tmp);
            }}
            className="numo-card"
            style={{ width: 38, height: 38, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
          >
            <ArrowRightLeft size={16} color="#00796b" />
          </button>
          <div className="numo-card" style={{ flex: 1, padding: 12 }}>
            <div style={{ fontSize: 11, color: '#5c4636', fontWeight: 700 }}>To Account</div>
            <select
              className="numo-input"
              style={{ marginTop: 4 }}
              value={toAccountId}
              onChange={(e) => setToAccountId(e.target.value)}
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.name} ({formatKes(Number(a.balance))})
                </option>
              ))}
            </select>
          </div>
        </div>
      )}

      {/* Amount Input + Calculator Button */}
      <div className="numo-card" style={{ padding: 16, marginBottom: 14 }}>
        <div style={{ fontSize: 12, fontWeight: 700, color: '#5c4636', marginBottom: 6 }}>
          {mode === 'income' ? 'Gross Income Amount' : mode === 'transfer' ? 'Transfer Amount' : 'Amount'}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span style={{ fontSize: 22, fontWeight: 800, color: accent }}>KES</span>
          <input
            type="number"
            min="0"
            step="0.01"
            placeholder="0"
            value={amount}
            onChange={(e) => {
              setAmount(e.target.value);
              setError(null);
            }}
            style={{
              flex: 1,
              fontSize: 28,
              fontWeight: 800,
              border: 'none',
              background: 'transparent',
              outline: 'none',
              color: '#2d1f14',
            }}
          />
          <button
            type="button"
            onClick={() => setShowCalc(true)}
            aria-label="Open Calculator"
            style={{
              width: 40,
              height: 40,
              borderRadius: 12,
              border: 'none',
              background: '#f59e0b',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
            }}
          >
            <Calculator size={20} />
          </button>
        </div>
      </div>

      {/* Mode Specific Forms */}
      {mode === 'expense' && (
        <div style={{ display: 'grid', gap: 12 }}>
          <div className="numo-card" style={{ padding: 14 }}>
            <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 8 }}>Expense Category</div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {EXPENSE_CATS.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setExpenseCategory(c)}
                  className="numo-pill"
                  style={{
                    background: expenseCategory === c ? '#d81b60' : '#f1e5cf',
                    color: expenseCategory === c ? '#fff' : '#2d1f14',
                  }}
                >
                  {c}
                </button>
              ))}
            </div>
          </div>

          <div className="numo-card" style={{ padding: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Account</label>
            <select
              className="numo-input"
              value={expenseAccountId}
              onChange={(e) => setExpenseAccountId(e.target.value)}
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.name} — {formatKes(Number(a.balance))}
                </option>
              ))}
            </select>
          </div>
        </div>
      )}

      {mode === 'income' && (
        <div style={{ display: 'grid', gap: 12 }}>
          <div className="numo-card" style={{ padding: 14 }}>
            <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 8 }}>Income Category</div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {INCOME_CATS.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setIncomeCategory(c)}
                  className="numo-pill"
                  style={{
                    background: incomeCategory === c ? '#1b8a5a' : '#f1e5cf',
                    color: incomeCategory === c ? '#fff' : '#2d1f14',
                  }}
                >
                  {c}
                </button>
              ))}
            </div>
          </div>

          {/* Tax / Deductions */}
          <div className="numo-card" style={{ padding: 14 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <strong style={{ fontSize: 13 }}>Tax / Deductions</strong>
              <input type="checkbox" checked={applyTax} onChange={(e) => setApplyTax(e.target.checked)} />
            </div>
            {applyTax && (
              <div style={{ marginTop: 10, display: 'grid', gap: 8 }}>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button type="button" onClick={() => setDeductionIsPct(true)} className="numo-pill" style={{ background: deductionIsPct ? '#5b3a29' : '#f1e5cf', color: deductionIsPct ? '#fff' : '#2d1f14' }}>
                    Percentage (%)
                  </button>
                  <button type="button" onClick={() => setDeductionIsPct(false)} className="numo-pill" style={{ background: !deductionIsPct ? '#5b3a29' : '#f1e5cf', color: !deductionIsPct ? '#fff' : '#2d1f14' }}>
                    Fixed KES
                  </button>
                </div>
                <input
                  type="number"
                  min="0"
                  className="numo-input"
                  value={taxValue}
                  onChange={(e) => setTaxValue(e.target.value)}
                  placeholder={deductionIsPct ? 'Percentage e.g. 18' : 'Fixed KES'}
                />
                <div style={{ fontSize: 12, display: 'grid', gap: 4, borderTop: '1px solid rgba(91,58,41,0.1)', paddingTop: 8 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Gross Income</span><strong>{formatKes(gross)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#d81b60' }}>
                    <span>Deductions</span><strong>-{formatKes(deductionAmount)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#1b8a5a', fontSize: 14 }}>
                    <strong>Net Credited</strong><strong>{formatKes(netIncome)}</strong>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Destination / Split Deposit */}
          <div className="numo-card" style={{ padding: 14 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
              <strong style={{ fontSize: 13 }}>Split Deposit Across Accounts</strong>
              <input type="checkbox" checked={splitDeposit} onChange={(e) => setSplitDeposit(e.target.checked)} />
            </div>
            {!splitDeposit ? (
              <select
                className="numo-input"
                value={incomeAccountId}
                onChange={(e) => setIncomeAccountId(e.target.value)}
              >
                {accounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.name} — {formatKes(Number(a.balance))}
                  </option>
                ))}
              </select>
            ) : (
              <div style={{ display: 'grid', gap: 8 }}>
                {accounts.map((a) => (
                  <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
                    <span style={{ fontSize: 13, fontWeight: 700 }}>{a.name}</span>
                    <input
                      type="number"
                      min="0"
                      className="numo-input"
                      style={{ width: 140 }}
                      placeholder="KES 0"
                      value={splitMap[a.id] || ''}
                      onChange={(e) => setSplitMap((prev) => ({ ...prev, [a.id]: e.target.value }))}
                    />
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="numo-card" style={{ padding: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Recurring Schedule</label>
            <select className="numo-input" value={incomeRecurring} onChange={(e) => setIncomeRecurring(e.target.value)}>
              <option value="ONE_TIME">One-time</option>
              <option value="WEEKLY">Weekly</option>
              <option value="BI_WEEKLY">Bi-weekly</option>
              <option value="MONTHLY">Monthly</option>
            </select>
          </div>
        </div>
      )}

      {mode === 'transfer' && (
        <div style={{ display: 'grid', gap: 12 }}>
          <div className="numo-card" style={{ padding: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Transfer Fee (KES, Optional)</label>
            <input type="number" min="0" className="numo-input" value={transferFee} onChange={(e) => setTransferFee(e.target.value)} />
          </div>
          <div className="numo-card" style={{ padding: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Reference (Optional)</label>
            <input type="text" className="numo-input" placeholder="e.g. Rent Share" value={transferRef} onChange={(e) => setTransferRef(e.target.value)} />
          </div>
          <div className="numo-card" style={{ padding: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: 13, fontWeight: 700 }}>Total Source Debit</span>
            <strong style={{ fontSize: 15, color: '#00796b' }}>{formatKes(totalSourceDebit)}</strong>
          </div>
          <div className="numo-card" style={{ padding: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Recurring Transfer</label>
            <select className="numo-input" value={transferRecurring} onChange={(e) => setTransferRecurring(e.target.value)}>
              <option value="ONE_TIME">One-time</option>
              <option value="WEEKLY">Weekly</option>
              <option value="MONTHLY">Monthly</option>
            </select>
          </div>
        </div>
      )}

      {/* Shared Date, Description & Receipt URL */}
      {mode !== 'transfer' && (
        <div style={{ display: 'grid', gap: 12, marginTop: 12 }}>
          <div className="numo-card" style={{ padding: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Date</label>
            <input type="date" className="numo-input" value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
          <div className="numo-card" style={{ padding: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Description</label>
            <input
              type="text"
              className="numo-input"
              placeholder={mode === 'income' ? 'e.g. Monthly Salary' : 'e.g. Lunch at Java House'}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <div className="numo-card" style={{ padding: 14 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 6 }}>
              <Paperclip size={15} color={accent} />
              <label style={{ fontSize: 12, fontWeight: 700 }}>Receipt / Document Reference (Optional)</label>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                type="text"
                className="numo-input"
                placeholder="Receipt note or URL"
                value={receiptUrl}
                onChange={(e) => setReceiptUrl(e.target.value)}
              />
              {receiptUrl && (
                <button type="button" onClick={() => setReceiptUrl('')} style={{ border: 'none', background: 'transparent', cursor: 'pointer' }}>
                  <X size={18} />
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {error && (
        <div style={{ marginTop: 12, padding: 12, borderRadius: 14, background: 'rgba(216,27,96,0.1)', color: '#d81b60', fontSize: 13, fontWeight: 700 }}>
          {error}
        </div>
      )}

      <button
        type="button"
        disabled={submitting}
        onClick={handleSubmit}
        style={{
          width: '100%',
          marginTop: 18,
          padding: '14px 20px',
          borderRadius: 22,
          border: 'none',
          background: accent,
          color: '#fff',
          fontSize: 15,
          fontWeight: 800,
          cursor: 'pointer',
          boxShadow: '0 8px 20px rgba(45,31,20,0.18)',
        }}
      >
        {submitting
          ? 'Saving...'
          : mode === 'income'
            ? 'Save Income'
            : mode === 'transfer'
              ? 'Review Transfer'
              : 'Save Transaction'}
      </button>

      {showCalc && (
        <NumoCalculatorModal
          initialAmount={gross}
          accentColor={accent}
          onClose={() => setShowCalc(false)}
          onApply={(val) => setAmount(String(val))}
        />
      )}

      {/* Review Transfer Modal */}
      {showReview && fromAcc && toAcc && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 12 }}>Review Transfer</h3>
            <div style={{ display: 'grid', gap: 6, fontSize: 13 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>From</span><strong>{fromAcc.name}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>To</span><strong>{toAcc.name}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Amount</span><strong>{formatKes(gross)}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Fee</span><strong>{formatKes(feeNum)}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: '#00796b' }}><strong>Total Debit</strong><strong>{formatKes(totalSourceDebit)}</strong></div>
              <hr style={{ border: 'none', borderTop: '1px solid rgba(91,58,41,0.1)', margin: '6px 0' }} />
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>New {fromAcc.name} Balance</span>
                <strong>{formatKes(Math.max(0, Number(fromAcc.balance) - totalSourceDebit))}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: '#1b8a5a' }}>
                <span>New {toAcc.name} Balance</span>
                <strong>{formatKes(Number(toAcc.balance) + gross)}</strong>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 10, marginTop: 18 }}>
              <button onClick={() => setShowReview(false)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>
                Edit
              </button>
              <button onClick={executeTransfer} disabled={submitting} className="numo-btn-primary" style={{ flex: 1 }}>
                {submitting ? 'Executing...' : 'Confirm Transfer'}
              </button>
            </div>
          </div>
        </div>
      )}
    </NumoShell>
  );
}

export default function AddTransactionPage() {
  return (
    <Suspense fallback={<div style={{ padding: 24 }}>Loading transaction form...</div>}>
      <AddTransactionContent />
    </Suspense>
  );
}
