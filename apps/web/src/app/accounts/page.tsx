'use client';

import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

export default function LinkedAccountsPage() {
  const { data, loading, refresh } = useDashboardData();
  const [showAdd, setShowAdd] = useState(false);
  const [name, setName] = useState('');
  const [provider, setProvider] = useState('M-Pesa');
  const [type, setType] = useState('MPESA');
  const [balance, setBalance] = useState('');

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    await fetch('/api/accounts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: name.trim(),
        provider: provider.trim(),
        type,
        balance: Number(balance || 0),
      }),
    });
    setName('');
    setBalance('');
    setShowAdd(false);
    refresh();
  };

  const handleArchive = async (id: string) => {
    await fetch(`/api/accounts?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
    refresh();
  };

  const accounts = data?.accounts || [];

  return (
    <NumoShell
      title="Linked Accounts"
      backHref="/profile"
      rightSlot={
        <button
          onClick={() => setShowAdd(true)}
          aria-label="Link Account"
          style={{ width: 36, height: 36, borderRadius: '50%', border: 'none', background: '#00796b', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
        >
          <Plus size={18} />
        </button>
      }
    >
      <div className="numo-hero-card" style={{ padding: 18, marginBottom: 16 }}>
        <div style={{ fontSize: 12, opacity: 0.9 }}>Total Active Account Balances</div>
        <div style={{ fontSize: 26, fontWeight: 800, marginTop: 4 }}>{formatKes(data?.totalAccountBalances ?? 0)}</div>
        <div style={{ fontSize: 12, opacity: 0.9, marginTop: 4 }}>
          Available Cash: {formatKes(data?.availableCash ?? 0)}
        </div>
      </div>

      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading accounts...</div>
      ) : (
        <div style={{ display: 'grid', gap: 10 }}>
          {accounts.map((a) => (
            <div key={a.id} className="numo-card" style={{ padding: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <strong style={{ fontSize: 15 }}>{a.name}</strong>
                <div style={{ fontSize: 11, color: '#5c4636' }}>{a.provider} &bull; {a.type} &bull; KES</div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <strong style={{ fontSize: 15, color: '#00796b' }}>{formatKes(Number(a.balance))}</strong>
                {!a.isDefaultCash && (
                  <button onClick={() => handleArchive(a.id)} aria-label="Archive account" style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#5c4636' }}>
                    <Trash2 size={15} />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {showAdd && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 12 }}>+ Link Account</h3>
            <form onSubmit={handleAdd} style={{ display: 'grid', gap: 10 }}>
              <input className="numo-input" placeholder="Account Name (e.g. M-PESA, KCB, Equity)" value={name} onChange={(e) => setName(e.target.value)} required />
              <input className="numo-input" placeholder="Provider (e.g. Safaricom, KCB Bank)" value={provider} onChange={(e) => setProvider(e.target.value)} />
              <select className="numo-input" value={type} onChange={(e) => setType(e.target.value)}>
                <option value="MPESA">M-PESA</option>
                <option value="BANK">Bank Account</option>
                <option value="CASH">Cash Wallet</option>
                <option value="SAVINGS">Savings</option>
              </select>
              <input type="number" className="numo-input" placeholder="Starting Balance (KES)" value={balance} onChange={(e) => setBalance(e.target.value)} />
              <div style={{ display: 'flex', gap: 10 }}>
                <button type="button" onClick={() => setShowAdd(false)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>Cancel</button>
                <button type="submit" className="numo-btn-primary" style={{ flex: 1 }}>Save Account</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </NumoShell>
  );
}
