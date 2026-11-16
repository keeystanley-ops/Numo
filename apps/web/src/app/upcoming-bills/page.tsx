'use client';

import { useMemo, useState } from 'react';
import { Calendar, CheckCircle2, Plus, Trash2 } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes, formatLocalDate } from '@/lib/currency';
import { DashboardPayload, useDashboardData } from '@/lib/useDashboard';

type BillRow = DashboardPayload['bills'][number];

const PRESETS = [
  { title: 'Rent', category: 'Housing', amount: '15000' },
  { title: 'KPLC Tokens', category: 'Bills & Utilities', amount: '2500' },
  { title: 'Water', category: 'Bills & Utilities', amount: '1200' },
  { title: 'Safaricom Home Fibre', category: 'Bills & Utilities', amount: '3000' },
  { title: 'Zuku', category: 'Bills & Utilities', amount: '2799' },
  { title: 'NHIF / SHIF', category: 'Health & Fitness', amount: '500' },
];

export default function UpcomingBillsPage() {
  const { data, loading, refresh } = useDashboardData();
  const bills = data?.bills || [];
  const payments = data?.billPayments || [];
  const accounts = data?.accounts || [];

  const [dirTab, setDirTab] = useState<'UPCOMING' | 'OVERDUE' | 'PAID'>('UPCOMING');
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [showCalendar, setShowCalendar] = useState(false);

  const [detailBill, setDetailBill] = useState<BillRow | null>(null);
  const [payBill, setPayBill] = useState<BillRow | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);

  // Add Bill form state
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('Bills & Utilities');
  const [amount, setAmount] = useState('');
  const [dueDate, setDueDate] = useState(() => new Date(Date.now() + 3 * 86400000).toISOString().slice(0, 10));
  const [recurrence, setRecurrence] = useState('MONTHLY');
  const [accountNumber, setAccountNumber] = useState('');
  const [autoPayPref, setAutoPayPref] = useState(false);

  // Pay Bill form state
  const [payAmount, setPayAmount] = useState('');
  const [payFee, setPayFee] = useState('0');
  const [payAccountId, setPayAccountId] = useState('');
  const [payError, setPayError] = useState<string | null>(null);
  const [paying, setPaying] = useState(false);

  const now = Date.now();
  const filteredBills = useMemo(() => {
    let list = bills;
    if (selectedDate) {
      list = list.filter((b) => new Date(b.dueDate).toISOString().slice(0, 10) === selectedDate);
    }
    if (dirTab === 'PAID') return list.filter((b) => b.isPaid);
    if (dirTab === 'OVERDUE') return list.filter((b) => !b.isPaid && new Date(b.dueDate).getTime() < now - 86400000);
    return list.filter((b) => !b.isPaid && new Date(b.dueDate).getTime() >= now - 86400000);
  }, [bills, dirTab, selectedDate, now]);

  const nextBill = filteredBills[0] || null;
  const otherBills = filteredBills.slice(1);

  const handleCreateBill = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || Number(amount) <= 0) return;
    await fetch('/api/bills', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: title.trim(),
        category,
        amount: Number(amount),
        dueDate,
        recurrence,
        accountNumber,
        autoPayPreference: autoPayPref,
      }),
    });
    setTitle('');
    setAmount('');
    setShowAddModal(false);
    refresh();
  };

  const handleDeleteBill = async (id: string) => {
    await fetch(`/api/bills?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
    setDetailBill(null);
    refresh();
  };

  const openPayModal = (bill: BillRow) => {
    const rem = Math.max(0, Number(bill.amount) - Number(bill.paidAmount || 0));
    setPayAmount(String(rem));
    setPayFee('0');
    setPayAccountId(accounts[0]?.id || '');
    setPayError(null);
    setPayBill(bill);
  };

  const handleConfirmPay = async () => {
    if (!payBill || paying) return;
    setPayError(null);
    const amt = Number(payAmount);
    if (amt <= 0) return setPayError('Enter a valid payment amount.');
    if (!payAccountId) return setPayError('Select an account.');

    setPaying(true);
    try {
      const res = await fetch('/api/bills/pay', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          billId: payBill.id,
          splits: [{ accountId: payAccountId, amount: amt }],
          fee: Number(payFee || 0),
        }),
      });
      const json = await res.json();
      if (!res.ok) throw new Error(json.error || 'Payment failed');
      setPayBill(null);
      setDetailBill(null);
      await refresh();
    } catch (err) {
      setPayError(err instanceof Error ? err.message : 'Payment failed');
    } finally {
      setPaying(false);
    }
  };

  const remainingOf = (b: BillRow) => Math.max(0, Number(b.amount) - Number(b.paidAmount || 0));

  return (
    <NumoShell
      title="Upcoming Bills"
      backHref="/dashboard"
      rightSlot={
        <button
          onClick={() => setShowCalendar(!showCalendar)}
          aria-label="Toggle Calendar"
          className="numo-card"
          style={{ width: 38, height: 38, borderRadius: 12, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
        >
          <Calendar size={18} color="#00796b" />
        </button>
      }
    >
      {/* Directory Tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
        {(['UPCOMING', 'OVERDUE', 'PAID'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setDirTab(t)}
            className="numo-pill"
            style={{
              background: dirTab === t ? '#2d1f14' : '#f1e5cf',
              color: dirTab === t ? '#fff' : '#2d1f14',
            }}
          >
            {t === 'UPCOMING' ? 'Upcoming' : t === 'OVERDUE' ? 'Overdue' : 'Paid History'}
          </button>
        ))}
      </div>

      {/* Calendar Filter */}
      {showCalendar && (
        <div className="numo-card" style={{ padding: 14, marginBottom: 14 }}>
          <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 6 }}>Filter by Due Date</div>
          <div style={{ display: 'flex', gap: 8 }}>
            <input
              type="date"
              className="numo-input"
              value={selectedDate || ''}
              onChange={(e) => setSelectedDate(e.target.value || null)}
            />
            {selectedDate && (
              <button onClick={() => setSelectedDate(null)} className="numo-pill" style={{ background: '#d81b60', color: '#fff' }}>
                Clear
              </button>
            )}
          </div>
        </div>
      )}

      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading bills...</div>
      ) : !nextBill ? (
        <div className="numo-card" style={{ padding: 24, textAlign: 'center' }}>
          <CheckCircle2 size={36} color="#1b8a5a" style={{ margin: '0 auto 8px' }} />
          <div style={{ fontWeight: 800 }}>No bills in this view</div>
          <p style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>Tap + below to schedule a bill.</p>
        </div>
      ) : (
        <>
          <div style={{ fontSize: 14, fontWeight: 800, marginBottom: 8 }}>
            {dirTab === 'PAID' ? 'Latest Paid Bill' : 'Next Bill'}
          </div>
          <div
            onClick={() => setDetailBill(nextBill)}
            className="numo-card"
            style={{ padding: 16, marginBottom: 16, cursor: 'pointer' }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontSize: 16, fontWeight: 800 }}>{nextBill.title}</div>
                <div style={{ fontSize: 12, color: '#5c4636' }}>
                  {nextBill.category} &bull; Due {formatLocalDate(nextBill.dueDate)}
                </div>
              </div>
              <span
                className="numo-pill"
                style={{
                  background: nextBill.isPaid ? 'rgba(27,138,90,0.14)' : 'rgba(216,27,96,0.12)',
                  color: nextBill.isPaid ? '#1b8a5a' : '#d81b60',
                }}
              >
                {nextBill.isPaid ? 'Paid' : Number(nextBill.paidAmount) > 0 ? 'Partial' : 'Unpaid'}
              </span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 14 }}>
              <div>
                <div style={{ fontSize: 22, fontWeight: 800 }}>
                  {formatKes(nextBill.isPaid ? Number(nextBill.amount) : remainingOf(nextBill))}
                </div>
                {Number(nextBill.paidAmount) > 0 && !nextBill.isPaid && (
                  <div style={{ fontSize: 11, color: '#1b8a5a' }}>
                    Paid {formatKes(Number(nextBill.paidAmount))} of {formatKes(Number(nextBill.amount))}
                  </div>
                )}
              </div>
              {!nextBill.isPaid && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    openPayModal(nextBill);
                  }}
                  className="numo-pill"
                  style={{ background: '#1b8a5a', color: '#fff', padding: '10px 16px', fontSize: 13 }}
                >
                  Pay Now
                </button>
              )}
            </div>
          </div>

          {otherBills.length > 0 && (
            <>
              <div style={{ fontSize: 14, fontWeight: 800, marginBottom: 8 }}>Other Bills</div>
              <div style={{ display: 'grid', gap: 10 }}>
                {otherBills.map((b) => (
                  <div
                    key={b.id}
                    onClick={() => setDetailBill(b)}
                    className="numo-card"
                    style={{ padding: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }}
                  >
                    <div>
                      <div style={{ fontWeight: 800, fontSize: 14 }}>{b.title}</div>
                      <div style={{ fontSize: 11, color: '#5c4636' }}>Due {formatLocalDate(b.dueDate)}</div>
                    </div>
                    <strong style={{ fontSize: 14 }}>{formatKes(b.isPaid ? Number(b.amount) : remainingOf(b))}</strong>
                  </div>
                ))}
              </div>
            </>
          )}
        </>
      )}

      {/* Payment History Section (Preserved even if bill is deleted) */}
      {payments.length > 0 && (
        <div style={{ marginTop: 20 }}>
          <div style={{ fontSize: 14, fontWeight: 800, marginBottom: 8 }}>Recent Payment History</div>
          <div style={{ display: 'grid', gap: 8 }}>
            {payments.slice(0, 5).map((p) => (
              <div key={p.id} className="numo-card" style={{ padding: 12, display: 'flex', justifyContent: 'space-between' }}>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 13 }}>{p.billTitle}</div>
                  <div style={{ fontSize: 11, color: '#5c4636' }}>
                    {formatLocalDate(p.paidAt)} &bull; {p.accountName}
                  </div>
                </div>
                <strong style={{ color: '#1b8a5a', fontSize: 13 }}>{formatKes(Number(p.amountPaid))}</strong>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Floating Add Bill Button */}
      <button
        onClick={() => setShowAddModal(true)}
        aria-label="Add Bill"
        style={{
          position: 'fixed',
          bottom: 92,
          right: 'calc(50% - 220px)',
          width: 54,
          height: 54,
          borderRadius: '50%',
          border: 'none',
          background: '#d81b60',
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          boxShadow: '0 10px 24px rgba(216,27,96,0.35)',
          zIndex: 25,
        }}
      >
        <Plus size={26} />
      </button>

      {/* Bill Details Modal */}
      {detailBill && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 400, padding: 20, background: '#fef9ef' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <strong style={{ fontSize: 17 }}>Bill Details</strong>
              <button onClick={() => setDetailBill(null)} style={{ border: 'none', background: 'transparent', cursor: 'pointer', fontWeight: 700 }}>
                Close
              </button>
            </div>
            <div style={{ textAlign: 'center', marginBottom: 14 }}>
              <div style={{ fontSize: 20, fontWeight: 800 }}>{detailBill.title}</div>
              <div style={{ fontSize: 12, color: '#5c4636' }}>{detailBill.category} &bull; {detailBill.recurrence}</div>
              <div style={{ fontSize: 26, fontWeight: 800, marginTop: 8 }}>{formatKes(remainingOf(detailBill))}</div>
              <div style={{ fontSize: 12, color: detailBill.isPaid ? '#1b8a5a' : '#d81b60', fontWeight: 700 }}>
                Status: {detailBill.isPaid ? 'Paid' : Number(detailBill.paidAmount) > 0 ? 'Partially Paid' : 'Unpaid'}
              </div>
            </div>

            <div style={{ display: 'flex', gap: 10 }}>
              <button
                onClick={() => handleDeleteBill(detailBill.id)}
                className="numo-card"
                style={{ padding: '10px 14px', color: '#d81b60', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}
              >
                <Trash2 size={15} /> Delete
              </button>
              {!detailBill.isPaid && (
                <button
                  onClick={() => openPayModal(detailBill)}
                  className="numo-btn-primary"
                  style={{ flex: 1, background: '#d81b60' }}
                >
                  Pay Bill Now
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Pay Bill Modal */}
      {payBill && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 70 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 400, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 4 }}>Pay {payBill.title}</h3>
            <p style={{ fontSize: 12, color: '#5c4636', marginBottom: 12 }}>
              Outstanding: {formatKes(remainingOf(payBill))}
            </p>
            <div style={{ display: 'grid', gap: 10 }}>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Payment Amount (KES — Full or Partial)</label>
                <input type="number" className="numo-input" value={payAmount} onChange={(e) => setPayAmount(e.target.value)} />
              </div>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Pay From Account</label>
                <select className="numo-input" value={payAccountId} onChange={(e) => setPayAccountId(e.target.value)}>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.name} ({formatKes(Number(a.balance))})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Fee (KES, Optional)</label>
                <input type="number" className="numo-input" value={payFee} onChange={(e) => setPayFee(e.target.value)} />
              </div>
              {payError && <div style={{ color: '#d81b60', fontSize: 12, fontWeight: 700 }}>{payError}</div>}
              <div style={{ display: 'flex', gap: 10, marginTop: 6 }}>
                <button onClick={() => setPayBill(null)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>
                  Cancel
                </button>
                <button onClick={handleConfirmPay} disabled={paying} className="numo-btn-primary" style={{ flex: 1 }}>
                  {paying ? 'Paying...' : 'Confirm Pay'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Add New Bill Modal */}
      {showAddModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 70 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 400, padding: 20, background: '#fef9ef', maxHeight: '88vh', overflowY: 'auto' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 10 }}>Add New Bill</h3>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 12 }}>
              {PRESETS.map((p) => (
                <button
                  key={p.title}
                  type="button"
                  onClick={() => {
                    setTitle(p.title);
                    setCategory(p.category);
                    setAmount(p.amount);
                  }}
                  className="numo-pill"
                  style={{ background: '#f1e5cf', color: '#2d1f14' }}
                >
                  {p.title}
                </button>
              ))}
            </div>
            <form onSubmit={handleCreateBill} style={{ display: 'grid', gap: 10 }}>
              <input className="numo-input" placeholder="Bill Name" value={title} onChange={(e) => setTitle(e.target.value)} required />
              <input type="number" className="numo-input" placeholder="Amount (KES)" value={amount} onChange={(e) => setAmount(e.target.value)} required />
              <input className="numo-input" placeholder="Paybill / Account Ref (Optional)" value={accountNumber} onChange={(e) => setAccountNumber(e.target.value)} />
              <input type="date" className="numo-input" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
              <select className="numo-input" value={recurrence} onChange={(e) => setRecurrence(e.target.value)}>
                <option value="ONE_TIME">One-time</option>
                <option value="MONTHLY">Monthly</option>
                <option value="QUARTERLY">Quarterly</option>
                <option value="ANNUAL">Annual</option>
              </select>
              <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 12 }}>
                <span>Auto-Pay Preference (Reminder only)</span>
                <input type="checkbox" checked={autoPayPref} onChange={(e) => setAutoPayPref(e.target.checked)} />
              </label>
              <div style={{ display: 'flex', gap: 10, marginTop: 6 }}>
                <button type="button" onClick={() => setShowAddModal(false)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>
                  Cancel
                </button>
                <button type="submit" className="numo-btn-primary" style={{ flex: 1, background: '#d81b60' }}>
                  Save Bill
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </NumoShell>
  );
}
