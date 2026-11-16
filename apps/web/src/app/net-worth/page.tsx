'use client';

import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

export default function NetWorthPage() {
  const { data, loading, refresh } = useDashboardData();
  const [showModal, setShowModal] = useState<'ASSET' | 'LIABILITY' | null>(null);
  const [name, setName] = useState('');
  const [category, setCategory] = useState('Property');
  const [value, setValue] = useState('');

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || Number(value) <= 0 || !showModal) return;
    await fetch('/api/net-worth', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(
        showModal === 'ASSET'
          ? { kind: 'ASSET', name: name.trim(), category, currentValue: Number(value) }
          : { kind: 'LIABILITY', name: name.trim(), category, currentBalance: Number(value) }
      ),
    });
    setName('');
    setValue('');
    setShowModal(null);
    refresh();
  };

  const handleDelete = async (id: string, kind: 'ASSET' | 'LIABILITY') => {
    await fetch(`/api/net-worth?id=${encodeURIComponent(id)}&kind=${kind}`, { method: 'DELETE' });
    refresh();
  };

  return (
    <NumoShell title="Net Worth" backHref="/dashboard">
      <div className="numo-hero-card" style={{ padding: 18, marginBottom: 16 }}>
        <div style={{ fontSize: 12, opacity: 0.9 }}>Total Net Worth (Assets &minus; Liabilities)</div>
        <div style={{ fontSize: 28, fontWeight: 800, marginTop: 4 }}>{formatKes(data?.totalNetWorth ?? 0)}</div>
        <div style={{ fontSize: 12, opacity: 0.9, marginTop: 6 }}>
          Assets: {formatKes(data?.totalAssets ?? 0)} &bull; Liabilities: {formatKes(data?.totalLiabilities ?? 0)}
        </div>
      </div>

      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading net worth...</div>
      ) : (
        <>
          {/* Assets Breakdown (No double counting of connected accounts) */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <h2 style={{ fontSize: 15, fontWeight: 800 }}>Assets</h2>
            <button onClick={() => { setCategory('Property'); setShowModal('ASSET'); }} className="numo-pill" style={{ background: '#1b8a5a', color: '#fff', display: 'flex', alignItems: 'center', gap: 4 }}>
              <Plus size={13} /> Add Asset
            </button>
          </div>
          <div style={{ display: 'grid', gap: 8, marginBottom: 16 }}>
            <div className="numo-card" style={{ padding: 12, display: 'flex', justifyContent: 'space-between' }}>
              <span>Connected Cash &amp; Bank Accounts</span>
              <strong style={{ color: '#00796b' }}>{formatKes(data?.totalAccountBalances ?? 0)}</strong>
            </div>
            <div className="numo-card" style={{ padding: 12, display: 'flex', justifyContent: 'space-between' }}>
              <span>Investment Portfolio</span>
              <strong style={{ color: '#00796b' }}>{formatKes(data?.totalPortfolioValuation ?? 0)}</strong>
            </div>
            {(data?.assets || []).map((a) => (
              <div key={a.id} className="numo-card" style={{ padding: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <strong style={{ fontSize: 13 }}>{a.name}</strong>
                  <div style={{ fontSize: 11, color: '#5c4636' }}>{a.category}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <strong>{formatKes(Number(a.currentValue))}</strong>
                  <button onClick={() => handleDelete(a.id, 'ASSET')} style={{ border: 'none', background: 'transparent', cursor: 'pointer' }}>
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Liabilities Breakdown */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <h2 style={{ fontSize: 15, fontWeight: 800 }}>Liabilities</h2>
            <button onClick={() => { setCategory('Loan'); setShowModal('LIABILITY'); }} className="numo-pill" style={{ background: '#d81b60', color: '#fff', display: 'flex', alignItems: 'center', gap: 4 }}>
              <Plus size={13} /> Add Liability
            </button>
          </div>
          <div style={{ display: 'grid', gap: 8 }}>
            {(data?.debts || []).map((d) => (
              <div key={d.id} className="numo-card" style={{ padding: 12, display: 'flex', justifyContent: 'space-between' }}>
                <span>{d.name} (Debt)</span>
                <strong style={{ color: '#d81b60' }}>{formatKes(Number(d.currentBalance))}</strong>
              </div>
            ))}
            {(data?.liabilities || []).map((l) => (
              <div key={l.id} className="numo-card" style={{ padding: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <strong style={{ fontSize: 13 }}>{l.name}</strong>
                  <div style={{ fontSize: 11, color: '#5c4636' }}>{l.category}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <strong style={{ color: '#d81b60' }}>{formatKes(Number(l.currentBalance))}</strong>
                  <button onClick={() => handleDelete(l.id, 'LIABILITY')} style={{ border: 'none', background: 'transparent', cursor: 'pointer' }}>
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            ))}
            {(data?.debts || []).length === 0 && (data?.liabilities || []).length === 0 && (
              <div className="numo-card" style={{ padding: 14, fontSize: 13, color: '#5c4636' }}>
                No liabilities recorded.
              </div>
            )}
          </div>
        </>
      )}

      {showModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 12 }}>
              {showModal === 'ASSET' ? 'Add Asset' : 'Add Liability'}
            </h3>
            <form onSubmit={handleSave} style={{ display: 'grid', gap: 10 }}>
              <input className="numo-input" placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} required />
              <input className="numo-input" placeholder="Category" value={category} onChange={(e) => setCategory(e.target.value)} required />
              <input type="number" className="numo-input" placeholder="Amount (KES)" value={value} onChange={(e) => setValue(e.target.value)} required />
              <div style={{ display: 'flex', gap: 10 }}>
                <button type="button" onClick={() => setShowModal(null)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>Cancel</button>
                <button type="submit" className="numo-btn-primary" style={{ flex: 1 }}>Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </NumoShell>
  );
}
