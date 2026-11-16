'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ArrowRight, ShieldCheck, Sparkles, Wallet } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';

export default function WelcomeSplashPage() {
  const router = useRouter();
  const [animDone, setAnimDone] = useState(false);
  const [checkingUser, setCheckingUser] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => setAnimDone(true), 1400);
    fetch('/api/profile')
      .then((r) => (r.ok ? r.json() : null))
      .then((json) => {
        if (json?.user?.onboardingCompleted) {
          router.replace('/dashboard');
        }
      })
      .catch(() => {})
      .finally(() => setCheckingUser(false));
    return () => clearTimeout(timer);
  }, [router]);

  return (
    <NumoShell hideBottomNav>
      <div
        style={{
          minHeight: '88vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'space-between',
          paddingTop: 28,
          paddingBottom: 18,
        }}
      >
        <div style={{ textAlign: 'center' }}>
          {/* Animated Numo N Logo Formation */}
          <div
            className="numo-card"
            style={{
              width: 108,
              height: 108,
              margin: '0 auto 20px',
              borderRadius: 30,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: 'linear-gradient(145deg, #00796b, #14b8a6)',
              boxShadow: '0 16px 34px rgba(0, 121, 107, 0.28)',
            }}
          >
            <svg width="62" height="62" viewBox="0 0 64 64" fill="none" aria-label="Numo N Logo">
              <path
                d="M16 50V14L48 50V14"
                stroke="#FFFFFF"
                strokeWidth="7.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="numo-n-stroke"
              />
            </svg>
          </div>

          <span
            style={{
              fontSize: 12,
              fontWeight: 800,
              letterSpacing: 1.5,
              color: '#00796b',
              textTransform: 'uppercase',
            }}
          >
            Numo Personal Finance OS
          </span>
          <h1 style={{ fontSize: 30, fontWeight: 800, marginTop: 8, lineHeight: 1.15 }}>
            Track. Plan. Grow.
          </h1>
          <p style={{ fontSize: 14, color: '#5c4636', marginTop: 10, lineHeight: 1.5 }}>
            Your unified Kenyan Shilling (KES) money management, connected accounts, bills, and wealth operating system.
          </p>
        </div>

        <div style={{ display: 'grid', gap: 12, margin: '24px 0' }}>
          <div className="numo-card" style={{ padding: 14, display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ width: 40, height: 40, borderRadius: 12, background: 'rgba(20,184,166,0.14)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#00796b' }}>
              <Wallet size={20} />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 14 }}>M-PESA, KCB, Equity &amp; Cash</div>
              <div style={{ fontSize: 12, color: '#5c4636' }}>One source of truth with atomic balance sync</div>
            </div>
          </div>

          <div className="numo-card" style={{ padding: 14, display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ width: 40, height: 40, borderRadius: 12, background: 'rgba(216,27,96,0.12)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#d81b60' }}>
              <ShieldCheck size={20} />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 14 }}>Double-Entry Neon Ledger</div>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Budgets, bills, goals, investments &amp; net worth</div>
            </div>
          </div>

          <div className="numo-card" style={{ padding: 14, display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ width: 40, height: 40, borderRadius: 12, background: 'rgba(245,158,11,0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#d97706' }}>
              <Sparkles size={20} />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 14 }}>Hey Numo AI Assistant</div>
              <div style={{ fontSize: 12, color: '#5c4636' }}>Hands-free Kenyan finance copilot</div>
            </div>
          </div>
        </div>

        <div style={{ display: 'grid', gap: 10, opacity: animDone ? 1 : 0.7, transition: 'opacity 0.3s' }}>
          <Link
            href="/onboarding"
            className="numo-btn-primary"
            style={{
              textDecoration: 'none',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
            }}
          >
            {checkingUser ? 'Loading...' : 'Get Started'} <ArrowRight size={17} />
          </Link>
          <div style={{ display: 'flex', gap: 10 }}>
            <Link
              href="/sign-in"
              className="numo-card"
              style={{
                flex: 1,
                textAlign: 'center',
                padding: '12px 14px',
                textDecoration: 'none',
                fontWeight: 700,
                fontSize: 14,
                color: '#2d1f14',
              }}
            >
              Sign In
            </Link>
            <Link
              href="/dashboard"
              className="numo-card"
              style={{
                flex: 1,
                textAlign: 'center',
                padding: '12px 14px',
                textDecoration: 'none',
                fontWeight: 700,
                fontSize: 14,
                color: '#00796b',
              }}
            >
              Open Dashboard
            </Link>
          </div>
        </div>
      </div>
    </NumoShell>
  );
}
