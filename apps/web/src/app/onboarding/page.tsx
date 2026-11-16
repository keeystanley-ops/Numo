'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { CheckCircle2, Landmark, ShieldCheck, Smartphone, Wallet } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes } from '@/lib/currency';

export default function OnboardingPage() {
  const router = useRouter();
  const [step, setStep] = useState<1 | 2 | 3 | 4>(1);
  const [fullName, setFullName] = useState('');
  const [mpesaBal, setMpesaBal] = useState('8500');
  const [kcbBal, setKcbBal] = useState('24500');
  const [equityBal, setEquityBal] = useState('12250');
  const [cashBal, setCashBal] = useState('3500');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCompleteOnboarding = async () => {
    setSaving(true);
    setError(null);
    try {
      const startingAccounts = [
        { name: 'M-PESA', type: 'MPESA', balance: Number(mpesaBal || 0) },
        { name: 'KCB', type: 'BANK', balance: Number(kcbBal || 0) },
        { name: 'Equity', type: 'BANK', balance: Number(equityBal || 0) },
        { name: 'Cash', type: 'CASH', balance: Number(cashBal || 0) },
      ].filter((a) => a.balance >= 0);

      const res = await fetch('/api/onboarding', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: fullName.trim() || undefined,
          primaryCurrency: 'KES',
          country: 'KE',
          preferredEnvironment: 'MONEY',
          startingAccounts,
        }),
      });
      const json = await res.json();
      if (!res.ok) throw new Error(json.error || 'Failed to save onboarding');
      setStep(4);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Onboarding failed');
    } finally {
      setSaving(false);
    }
  };

  return (
    <NumoShell title={`Setup (${step}/4)`} backHref={step === 1 ? '/' : undefined} hideBottomNav>
      {/* Step 1: Profile & KES Currency Confirmation */}
      {step === 1 && (
        <div className="numo-card" style={{ padding: 22, marginTop: 10 }}>
          <h2 style={{ fontSize: 21, fontWeight: 800 }}>Operating Currency &amp; Profile</h2>
          <p style={{ fontSize: 13, color: '#5c4636', marginTop: 4, marginBottom: 16 }}>
            Numo operates in Kenyan Shillings (KES) across all accounts, budgets, and reports.
          </p>

          <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Your Name</label>
          <input
            type="text"
            className="numo-input"
            placeholder="e.g. Stalon Kiprop"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
          />

          <div
            className="numo-card"
            style={{
              marginTop: 16,
              padding: 16,
              background: 'rgba(20, 184, 166, 0.1)',
              borderColor: '#14b8a6',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div>
              <div style={{ fontWeight: 800, fontSize: 15 }}>🇰🇪 Kenyan Shilling (KES)</div>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Default &amp; Enforced Operating Currency</div>
            </div>
            <CheckCircle2 color="#00796b" size={22} />
          </div>

          <button
            onClick={() => setStep(2)}
            className="numo-btn-primary"
            style={{ width: '100%', marginTop: 20 }}
          >
            Continue to Accounts
          </button>
        </div>
      )}

      {/* Step 2: Connect Financial Accounts */}
      {step === 2 && (
        <div className="numo-card" style={{ padding: 22, marginTop: 10 }}>
          <h2 style={{ fontSize: 21, fontWeight: 800 }}>Connect Financial Accounts</h2>
          <p style={{ fontSize: 13, color: '#5c4636', marginTop: 4, marginBottom: 16 }}>
            Enter your starting balances in KES. Starting balances represent existing assets and do not inflate monthly income.
          </p>

          <div style={{ display: 'grid', gap: 12 }}>
            <AccountBalanceInput icon={<Smartphone size={18} color="#1b8a5a" />} label="M-PESA Wallet (KES)" value={mpesaBal} onChange={setMpesaBal} />
            <AccountBalanceInput icon={<Landmark size={18} color="#00796b" />} label="KCB Bank (KES)" value={kcbBal} onChange={setKcbBal} />
            <AccountBalanceInput icon={<Landmark size={18} color="#d81b60" />} label="Equity Bank (KES)" value={equityBal} onChange={setEquityBal} />
            <AccountBalanceInput icon={<Wallet size={18} color="#d97706" />} label="Cash Wallet (KES)" value={cashBal} onChange={setCashBal} />
          </div>

          <div style={{ display: 'flex', gap: 10, marginTop: 20 }}>
            <button onClick={() => setStep(1)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>
              Back
            </button>
            <button onClick={() => setStep(3)} className="numo-btn-primary" style={{ flex: 2 }}>
              Review Connections
            </button>
          </div>
        </div>
      )}

      {/* Step 3: Account Connected Confirmation */}
      {step === 3 && (
        <div className="numo-card" style={{ padding: 22, marginTop: 10, textAlign: 'center' }}>
          <ShieldCheck size={48} color="#00796b" style={{ margin: '0 auto 10px' }} />
          <h2 style={{ fontSize: 21, fontWeight: 800 }}>Accounts Ready</h2>
          <p style={{ fontSize: 13, color: '#5c4636', marginTop: 4, marginBottom: 16 }}>
            Confirm your starting balances before saving to Neon PostgreSQL.
          </p>

          <div style={{ display: 'grid', gap: 8, textAlign: 'left', marginBottom: 18 }}>
            {[
              ['M-PESA', mpesaBal],
              ['KCB', kcbBal],
              ['Equity', equityBal],
              ['Cash', cashBal],
            ].map(([name, val]) => (
              <div key={name} className="numo-card" style={{ padding: '10px 14px', display: 'flex', justifyContent: 'space-between' }}>
                <strong>{name}</strong>
                <span style={{ fontWeight: 800, color: '#00796b' }}>{formatKes(Number(val || 0))}</span>
              </div>
            ))}
          </div>

          {error && <div style={{ color: '#d81b60', fontSize: 12, fontWeight: 700, marginBottom: 10 }}>{error}</div>}

          <div style={{ display: 'flex', gap: 10 }}>
            <button onClick={() => setStep(2)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>
              Edit
            </button>
            <button onClick={handleCompleteOnboarding} disabled={saving} className="numo-btn-primary" style={{ flex: 2 }}>
              {saving ? 'Saving...' : 'Confirm & Finish'}
            </button>
          </div>
        </div>
      )}

      {/* Step 4: All Set */}
      {step === 4 && (
        <div className="numo-card" style={{ padding: 28, marginTop: 20, textAlign: 'center' }}>
          <CheckCircle2 size={56} color="#1b8a5a" style={{ margin: '0 auto 12px' }} />
          <h2 style={{ fontSize: 24, fontWeight: 800 }}>You&apos;re All Set!</h2>
          <p style={{ fontSize: 13, color: '#5c4636', marginTop: 6, marginBottom: 22 }}>
            Your KES ledger and connected accounts are live. Welcome to Numo.
          </p>
          <button onClick={() => router.push('/dashboard')} className="numo-btn-primary" style={{ width: '100%' }}>
            Go to Home Dashboard
          </button>
        </div>
      )}
    </NumoShell>
  );
}

function AccountBalanceInput({
  icon,
  label,
  value,
  onChange,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div className="numo-card" style={{ padding: 12 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
        {icon}
        <span style={{ fontSize: 13, fontWeight: 700 }}>{label}</span>
      </div>
      <input
        type="number"
        min="0"
        className="numo-input"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}
