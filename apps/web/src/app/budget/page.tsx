'use client';

import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

export default function BudgetPage() {
  const { data, loading, refresh } = useDashboardData();
  const [category, setCategory] = useState('Food & Dining');
  const [limitAmount, setLimitAmount] = useState('');
  const [showAdd, setShowAdd] = useState(false);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (Number(limitAmount) <= 0) return;
    await fetch('/api/budgets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ category, limitAmount: Number(limitAmount) }),
    });
    setLimitAmount('');
    setShowAdd(false);
    refresh();
  };

  const handleDelete = async (id: string) => {
    await fetch(`/api/budgets?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
    refresh();
  };

  const budgets = data?.budgets || [];

  return (
    <NumoShell
      title="Budget Control"
      backHref="/dashboard"
      rightSlot={
        <button
          onClick={() => setShowAdd(true)}
          aria-label="Add Budget"
          style={{ width: 36, height: 36, borderRadius: '50%', border: 'none', background: '#00796b', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
        >
          <Plus size={18} />
        </button>
      }
    >
      <div className="numo-hero-card" style={{ padding: 18, marginBottom: 16 }}>
        <div style={{ fontSize: 12, opacity: 0.9 }}>Monthly Budget Target</div>
        <div style={{ fontSize: 26, fontWeight: 800, marginTop: 4 }}>{formatKes(data?.monthlyBudgetTarget ?? 0)}</div>
        <div style={{ fontSize: 12, opacity: 0.9, marginTop: 6 }}>
          Spent: {formatKes(data?.monthlySpent ?? 0)} &bull; Remaining: {formatKes(data?.totalRemaining ?? 0)}
        </div>
      </div>

      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading budgets...</div>
      ) : budgets.length === 0 ? (
        <div className="numo-card" style={{ padding: 22, textAlign: 'center' }}>
          <div style={{ fontWeight: 800 }}>No category budgets yet</div>
          <p style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>Tap + to set a monthly category limit.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 10 }}>
          {budgets.map((b) => {
            const pct = b.percentage;
            const color = pct >= 100 ? '#d81b60' : pct >= 80 ? '#f59e0b' : '#1b8a5a';
            return (
              <div key={b.id} className="numo-card" style={{ padding: 14 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <strong style={{ fontSize: 14 }}>{b.category}</strong>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ fontSize: 12, fontWeight: 800, color }}>{pct}%</span>
                    <button onClick={() => handleDelete(b.id)} style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#5c4636' }}>
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>
                <div style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>
                  {formatKes(b.spentAmount)} of {formatKes(Number(b.limitAmount))}
                </div>
                <div style={{ height: 8, borderRadius: 999, background: '#eadfcd', marginTop: 8, overflow: 'hidden' }}>
                  <div style={{ width: `${Math.min(100, pct)}%`, height: '100%', background: color, borderRadius: 999 }} />
                </div>
              </div>
            );
          })}
        </div>
      )}

      {showAdd && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 12 }}>Set Category Budget</h3>
            <form onSubmit={handleSave} style={{ display: 'grid', gap: 10 }}>
              <select className="numo-input" value={category} onChange={(e) => setCategory(e.target.value)}>
                {['Food & Dining', 'Transport', 'Shopping', 'Health & Fitness', 'Housing', 'Bills & Utilities', 'Entertainment', 'Other'].map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
              <input type="number" className="numo-input" placeholder="Monthly Limit (KES)" value={limitAmount} onChange={(e) => setLimitAmount(e.target.value)} required />
              <div style={{ display: 'flex', gap: 10 }}>
                <button type="button" onClick={() => setShowAdd(false)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>Cancel</button>
                <button type="submit" className="numo-btn-primary" style={{ flex: 1 }}>Save Budget</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </NumoShell>
  );
}
