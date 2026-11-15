'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';

interface DashboardData {
  currency: string;
  availableCash: number;
  monthlyIncome: number;
  monthlySpent: number;
  monthlyBudgetTarget: number;
  totalPortfolioValuation: number;
  totalDebt: number;
  totalNetWorth: number;
  accounts: Array<{ id: string; name: string; type: string; balance: string }>;
  transactions: Array<{ id: string; description: string; type: string; amount: string; date: string }>;
  budgets: Array<{ id: string; category: string; limitAmount: string }>;
  holdings: Array<{ id: string; name: string; assetClass: string; units: string; currentPrice: string }>;
}

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [envTab, setEnvTab] = useState<'MONEY' | 'INVESTMENTS'>('MONEY');

  useEffect(() => {
    fetch('/api/dashboard')
      .then(async (res) => {
        const json = await res.json();
        if (!res.ok) throw new Error(json.error || 'Failed to load dashboard');
        setData(json);
      })
      .catch((err) => setError(err.message));
  }, []);

  return (
    <main style={{ maxWidth: 980, margin: '0 auto', padding: '32px 20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <Link href="/" style={{ fontSize: 13, color: '#007764', fontWeight: 700, textDecoration: 'none' }}>
            &larr; Back to Console
          </Link>
          <h1 style={{ fontSize: 28, fontWeight: 800, marginTop: 4 }}>Numo Live Dashboard</h1>
        </div>
        <div style={{ display: 'flex', gap: 8, background: '#efe4cf', padding: 4, borderRadius: 16 }}>
          {(['MONEY', 'INVESTMENTS'] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setEnvTab(tab)}
              style={{
                padding: '8px 16px',
                borderRadius: 12,
                border: 'none',
                cursor: 'pointer',
                fontWeight: 700,
                background: envTab === tab ? '#007764' : 'transparent',
                color: envTab === tab ? '#fff' : '#5c4636',
              }}
            >
              {tab === 'MONEY' ? 'Money Management' : 'Investments'}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="numo-card" style={{ padding: 20, borderColor: '#e0911f', marginBottom: 20 }}>
          <strong>Database Setup Needed:</strong> {error}
        </div>
      )}

      {data && (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: 14, marginBottom: 24 }}>
            <div className="numo-card" style={{ padding: 18 }}>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Available Cash</div>
              <div style={{ fontSize: 24, fontWeight: 800, color: '#007764', marginTop: 4 }}>
                {data.currency} {data.availableCash.toLocaleString()}
              </div>
            </div>
            <div className="numo-card" style={{ padding: 18 }}>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Net Worth</div>
              <div style={{ fontSize: 24, fontWeight: 800, marginTop: 4 }}>
                {data.currency} {data.totalNetWorth.toLocaleString()}
              </div>
            </div>
            <div className="numo-card" style={{ padding: 18 }}>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Portfolio Valuation</div>
              <div style={{ fontSize: 24, fontWeight: 800, color: '#007764', marginTop: 4 }}>
                {data.currency} {data.totalPortfolioValuation.toLocaleString()}
              </div>
            </div>
            <div className="numo-card" style={{ padding: 18 }}>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Total Debt</div>
              <div style={{ fontSize: 24, fontWeight: 800, color: '#d91b7a', marginTop: 4 }}>
                {data.currency} {data.totalDebt.toLocaleString()}
              </div>
            </div>
          </div>

          {envTab === 'MONEY' ? (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 18 }}>
              <div className="numo-card" style={{ padding: 20 }}>
                <h2 style={{ fontSize: 17, fontWeight: 700, marginBottom: 12 }}>Linked Accounts</h2>
                {data.accounts.length === 0 ? (
                  <p style={{ fontSize: 13, color: '#5c4636' }}>No accounts yet. Complete onboarding or POST /api/accounts.</p>
                ) : (
                  data.accounts.map((a) => (
                    <div key={a.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid #efe4cf' }}>
                      <span>{a.name} ({a.type})</span>
                      <strong>{data.currency} {Number(a.balance).toLocaleString()}</strong>
                    </div>
                  ))
                )}
              </div>

              <div className="numo-card" style={{ padding: 20 }}>
                <h2 style={{ fontSize: 17, fontWeight: 700, marginBottom: 12 }}>Recent Transactions</h2>
                {data.transactions.length === 0 ? (
                  <p style={{ fontSize: 13, color: '#5c4636' }}>No transactions yet. POST /api/transactions to record one.</p>
                ) : (
                  data.transactions.map((t) => (
                    <div key={t.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid #efe4cf' }}>
                      <span>{t.description}</span>
                      <strong style={{ color: t.type === 'INCOME' ? '#007764' : '#2d1f14' }}>
                        {t.type === 'INCOME' ? '+' : '-'}{data.currency} {Number(t.amount).toLocaleString()}
                      </strong>
                    </div>
                  ))
                )}
              </div>
            </div>
          ) : (
            <div className="numo-card" style={{ padding: 20 }}>
              <h2 style={{ fontSize: 17, fontWeight: 700, marginBottom: 12 }}>Investment Holdings</h2>
              {data.holdings.length === 0 ? (
                <p style={{ fontSize: 13, color: '#5c4636' }}>No holdings yet. POST /api/investments to add NSE, MMF, or SACCO assets.</p>
              ) : (
                data.holdings.map((h) => (
                  <div key={h.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid #efe4cf' }}>
                    <span>{h.name} ({h.assetClass})</span>
                    <strong>{data.currency} {(Number(h.units) * Number(h.currentPrice)).toLocaleString()}</strong>
                  </div>
                ))
              )}
            </div>
          )}
        </>
      )}
    </main>
  );
}
