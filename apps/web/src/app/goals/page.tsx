'use client';

import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

const SUGGESTED = [
  { title: 'Emergency Fund (6 Months)', target: '250000' },
  { title: 'Holiday Savings', target: '120000' },
  { title: 'New Laptop', target: '100000' },
  { title: 'House Deposit', target: '500000' },
];

export default function GoalsPage() {
  const { data, loading, refresh } = useDashboardData();
  const [showAdd, setShowAdd] = useState(false);
  const [title, setTitle] = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [deltaMap, setDeltaMap] = useState<Record<string, string>>({});

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || Number(targetAmount) <= 0) return;
    await fetch('/api/goals', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: title.trim(), targetAmount: Number(targetAmount) }),
    });
    setTitle('');
    setTargetAmount('');
    setShowAdd(false);
    refresh();
  };

  const handleDelta = async (id: string, sign: 1 | -1) => {
    const val = Number(deltaMap[id] || 0);
    if (val <= 0) return;
    await fetch('/api/goals', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, delta: val * sign }),
    });
    setDeltaMap((p) => ({ ...p, [id]: '' }));
    refresh();
  };

  const handleDelete = async (id: string) => {
    await fetch(`/api/goals?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
    refresh();
  };

  const goals = data?.goals || [];

  return (
    <NumoShell
      title="Financial Goals"
      backHref="/dashboard"
      rightSlot={
        <button
          onClick={() => setShowAdd(true)}
          aria-label="Create Goal"
          style={{ width: 36, height: 36, borderRadius: '50%', border: 'none', background: '#00796b', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
        >
          <Plus size={18} />
        </button>
      }
    >
      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading goals...</div>
      ) : goals.length === 0 ? (
        <div className="numo-card" style={{ padding: 22, textAlign: 'center' }}>
          <div style={{ fontWeight: 800 }}>No savings goals yet</div>
          <p style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>Tap + to create a goal or pick a suggested milestone.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 12 }}>
          {goals.map((g) => {
            const saved = Number(g.savedAmount);
            const target = Math.max(1, Number(g.targetAmount));
            const pct = Math.min(100, Math.round((saved / target) * 100));
            return (
              <div key={g.id} className="numo-card" style={{ padding: 14 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <strong style={{ fontSize: 15 }}>{g.title}</strong>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ fontSize: 12, fontWeight: 800, color: '#00796b' }}>{pct}%</span>
                    <button onClick={() => handleDelete(g.id)} style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#5c4636' }}>
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>
                <div style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>
                  {formatKes(saved)} of {formatKes(target)}
                </div>
                <div style={{ height: 8, borderRadius: 999, background: '#eadfcd', marginTop: 8, overflow: 'hidden' }}>
                  <div style={{ width: `${pct}%`, height: '100%', background: '#1b8a5a', borderRadius: 999 }} />
                </div>
                <div style={{ display: 'flex', gap: 6, marginTop: 10 }}>
                  <input
                    type="number"
                    className="numo-input"
                    placeholder="Amount (KES)"
                    value={deltaMap[g.id] || ''}
                    onChange={(e) => setDeltaMap((p) => ({ ...p, [g.id]: e.target.value }))}
                  />
                  <button onClick={() => handleDelta(g.id, 1)} className="numo-pill" style={{ background: '#1b8a5a', color: '#fff' }}>
                    + Add
                  </button>
                  <button onClick={() => handleDelta(g.id, -1)} className="numo-pill" style={{ background: '#f1e5cf', color: '#d81b60' }}>
                    - Withdraw
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {showAdd && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 10 }}>Create Financial Goal</h3>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 10 }}>
              {SUGGESTED.map((s) => (
                <button
                  key={s.title}
                  type="button"
                  onClick={() => {
                    setTitle(s.title);
                    setTargetAmount(s.target);
                  }}
                  className="numo-pill"
                  style={{ background: '#f1e5cf', color: '#2d1f14' }}
                >
                  {s.title}
                </button>
              ))}
            </div>
            <form onSubmit={handleCreate} style={{ display: 'grid', gap: 10 }}>
              <input className="numo-input" placeholder="Goal Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
              <input type="number" className="numo-input" placeholder="Target Amount (KES)" value={targetAmount} onChange={(e) => setTargetAmount(e.target.value)} required />
              <div style={{ display: 'flex', gap: 10 }}>
                <button type="button" onClick={() => setShowAdd(false)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>Cancel</button>
                <button type="submit" className="numo-btn-primary" style={{ flex: 1 }}>Save Goal</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </NumoShell>
  );
}
