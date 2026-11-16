'use client';

import { useMemo, useState } from 'react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

export default function AnalyticsPage() {
  const { data, loading } = useDashboardData();
  const [range, setRange] = useState<'MONTH' | '30D' | 'ALL'>('MONTH');

  const txs = useMemo(() => {
    const list = data?.transactions || [];
    const now = Date.now();
    if (range === '30D') return list.filter((t) => now - new Date(t.date).getTime() <= 30 * 86400000);
    if (range === 'MONTH') {
      const m = new Date();
      return list.filter((t) => {
        const d = new Date(t.date);
        return d.getMonth() === m.getMonth() && d.getFullYear() === m.getFullYear();
      });
    }
    return list;
  }, [data, range]);

  const income = txs.filter((t) => t.type === 'INCOME').reduce((s, t) => s + Number(t.amount), 0);
  const expense = txs.filter((t) => t.type === 'EXPENSE').reduce((s, t) => s + Number(t.amount), 0);

  const categoryTotals = useMemo(() => {
    const map = new Map<string, number>();
    for (const t of txs) {
      if (t.type !== 'EXPENSE') continue;
      for (const sp of t.splits || [{ category: 'Other', amount: t.amount }]) {
        map.set(sp.category, (map.get(sp.category) || 0) + Number(sp.amount));
      }
    }
    return [...map.entries()].sort((a, b) => b[1] - a[1]);
  }, [txs]);

  return (
    <NumoShell title="Analytics" backHref="/dashboard">
      <div style={{ display: 'flex', gap: 8, marginBottom: 14 }}>
        {[
          ['MONTH', 'This Month'],
          ['30D', 'Last 30 Days'],
          ['ALL', 'All Time'],
        ].map(([k, lbl]) => (
          <button
            key={k}
            onClick={() => setRange(k as 'MONTH' | '30D' | 'ALL')}
            className="numo-pill"
            style={{
              background: range === k ? '#00796b' : '#f1e5cf',
              color: range === k ? '#fff' : '#2d1f14',
            }}
          >
            {lbl}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading analytics...</div>
      ) : (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
            <div className="numo-card" style={{ padding: 14 }}>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Total Income</div>
              <div style={{ fontSize: 18, fontWeight: 800, color: '#1b8a5a', marginTop: 4 }}>{formatKes(income)}</div>
            </div>
            <div className="numo-card" style={{ padding: 14 }}>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Total Expenses</div>
              <div style={{ fontSize: 18, fontWeight: 800, color: '#d81b60', marginTop: 4 }}>{formatKes(expense)}</div>
            </div>
          </div>

          <div className="numo-card" style={{ padding: 16, marginBottom: 16 }}>
            <div style={{ fontSize: 15, fontWeight: 800, marginBottom: 10 }}>Spending by Category</div>
            {categoryTotals.length === 0 ? (
              <div style={{ fontSize: 13, color: '#5c4636' }}>No expense data in this range.</div>
            ) : (
              <div style={{ display: 'grid', gap: 10 }}>
                {categoryTotals.map(([cat, amt]) => {
                  const pct = expense > 0 ? Math.round((amt / expense) * 100) : 0;
                  return (
                    <div key={cat}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
                        <strong>{cat}</strong>
                        <span>{formatKes(amt)} ({pct}%)</span>
                      </div>
                      <div style={{ height: 8, borderRadius: 999, background: '#eadfcd', overflow: 'hidden' }}>
                        <div style={{ width: `${pct}%`, height: '100%', background: '#00796b', borderRadius: 999 }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="numo-card" style={{ padding: 16 }}>
            <div style={{ fontSize: 15, fontWeight: 800, marginBottom: 8 }}>Account Activity Summary</div>
            {(data?.accounts || []).map((a) => (
              <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 13 }}>
                <span>{a.name}</span>
                <strong>{formatKes(Number(a.balance))}</strong>
              </div>
            ))}
          </div>
        </>
      )}
    </NumoShell>
  );
}
