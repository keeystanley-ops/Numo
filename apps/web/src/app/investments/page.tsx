'use client';

import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

export default function InvestmentsPage() {
  const { data, loading, refresh } = useDashboardData();
  const [showAdd, setShowAdd] = useState(false);
  const [name, setName] = useState('');
  const [assetClass, setAssetClass] = useState('MMF');
  const [units, setUnits] = useState('1');
  const [purchasePrice, setPurchasePrice] = useState('');
  const [currentPrice, setCurrentPrice] = useState('');

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || Number(currentPrice || purchasePrice) <= 0) return;
    await fetch('/api/investments', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: name.trim(),
        assetClass,
        units: Number(units || 1),
        purchasePrice: Number(purchasePrice || currentPrice),
        currentPrice: Number(currentPrice || purchasePrice),
      }),
    });
    setName('');
    setPurchasePrice('');
    setCurrentPrice('');
    setShowAdd(false);
    refresh();
  };

  const handleDelete = async (id: string) => {
    await fetch(`/api/investments?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
    refresh();
  };

  const holdings = data?.holdings || [];
  const totalVal = data?.totalPortfolioValuation ?? 0;
  const invested = data?.totalInvestedCapital ?? 0;
  const ret = totalVal - invested;

  return (
    <NumoShell
      title="Investments"
      backHref="/dashboard"
      rightSlot={
        <button
          onClick={() => setShowAdd(true)}
          aria-label="Add Investment"
          style={{ width: 36, height: 36, borderRadius: '50%', border: 'none', background: '#00796b', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
        >
          <Plus size={18} />
        </button>
      }
    >
      <div className="numo-hero-card" style={{ padding: 18, marginBottom: 16 }}>
        <div style={{ fontSize: 12, opacity: 0.9 }}>Total Portfolio Valuation</div>
        <div style={{ fontSize: 26, fontWeight: 800, marginTop: 4 }}>{formatKes(totalVal)}</div>
        <div style={{ fontSize: 12, opacity: 0.9, marginTop: 6 }}>
          Invested: {formatKes(invested)} &bull; Return: {ret >= 0 ? '+' : ''}{formatKes(ret)}
        </div>
      </div>

      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading portfolio...</div>
      ) : holdings.length === 0 ? (
        <div className="numo-card" style={{ padding: 22, textAlign: 'center' }}>
          <div style={{ fontWeight: 800 }}>No investment holdings yet</div>
          <p style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>Add NSE stocks, MMF, SACCO shares, or Treasury Bills.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 10 }}>
          {holdings.map((h) => {
            const val = Number(h.units) * Number(h.currentPrice);
            const cost = Number(h.units) * Number(h.purchasePrice);
            const gain = val - cost;
            return (
              <div key={h.id} className="numo-card" style={{ padding: 14 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong style={{ fontSize: 14 }}>{h.name}</strong>
                    <div style={{ fontSize: 11, color: '#5c4636' }}>{h.assetClass} &bull; {Number(h.units)} units</div>
                  </div>
                  <div style={{ textAlign: 'right', display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div>
                      <div style={{ fontWeight: 800, fontSize: 14 }}>{formatKes(val)}</div>
                      <div style={{ fontSize: 11, color: gain >= 0 ? '#1b8a5a' : '#d81b60', fontWeight: 700 }}>
                        {gain >= 0 ? '+' : ''}{formatKes(gain)}
                      </div>
                    </div>
                    <button onClick={() => handleDelete(h.id)} style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#5c4636' }}>
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {showAdd && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 12 }}>Add Investment Holding</h3>
            <form onSubmit={handleAdd} style={{ display: 'grid', gap: 10 }}>
              <input className="numo-input" placeholder="Holding Name (e.g. Sanlam MMF)" value={name} onChange={(e) => setName(e.target.value)} required />
              <select className="numo-input" value={assetClass} onChange={(e) => setAssetClass(e.target.value)}>
                <option value="MMF">Money Market Fund (MMF)</option>
                <option value="STOCK_NSE">NSE Stocks</option>
                <option value="SACCO">SACCO Shares</option>
                <option value="TBILL">Treasury Bill</option>
                <option value="BOND">Treasury Bond</option>
                <option value="CUSTOM">Other</option>
              </select>
              <input type="number" step="0.01" className="numo-input" placeholder="Units" value={units} onChange={(e) => setUnits(e.target.value)} required />
              <input type="number" step="0.01" className="numo-input" placeholder="Purchase Price (KES)" value={purchasePrice} onChange={(e) => setPurchasePrice(e.target.value)} required />
              <input type="number" step="0.01" className="numo-input" placeholder="Current Valuation per Unit (KES)" value={currentPrice} onChange={(e) => setCurrentPrice(e.target.value)} required />
              <div style={{ display: 'flex', gap: 10 }}>
                <button type="button" onClick={() => setShowAdd(false)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>Cancel</button>
                <button type="submit" className="numo-btn-primary" style={{ flex: 1 }}>Save Holding</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </NumoShell>
  );
}
