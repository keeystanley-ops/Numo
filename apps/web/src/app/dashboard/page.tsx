'use client';

import { useState } from 'react';
import Link from 'next/link';
import {
  ArrowDownLeft,
  ArrowRightLeft,
  ArrowUpRight,
  Bell,
  Eye,
  EyeOff,
  Plus,
  Receipt,
  TrendingUp,
  Wallet,
} from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatGreeting, formatKes, formatLocalDate } from '@/lib/currency';
import { useDashboardData } from '@/lib/useDashboard';

export default function DashboardPage() {
  const { data, loading, error, refresh } = useDashboardData();
  const [hideBalances, setHideBalances] = useState(false);
  const [showNotifs, setShowNotifs] = useState(false);

  const isHidden = hideBalances || Boolean(data?.user?.privacyHideBalances);
  const showMoney = (val: number) => (isHidden ? 'KES ••••••' : formatKes(val));

  const greeting = formatGreeting(data?.user, loading);
  const monthlyIncome = data?.monthlyIncome ?? 0;
  const monthlySpent = data?.monthlySpent ?? 0;
  const netFlow = data?.netCashFlow ?? 0;
  const totalFlow = Math.max(1, monthlyIncome + monthlySpent);
  const incomePct = Math.min(95, Math.max(5, Math.round((monthlyIncome / totalFlow) * 100)));

  const budgetPct = data?.budgetConsumptionPercent ?? 0;
  const budgetStatus =
    budgetPct >= 100 ? 'Over Budget' : budgetPct >= 80 ? 'Nearing Limit' : 'On Track';
  const budgetColor =
    budgetPct >= 100 ? '#d81b60' : budgetPct >= 80 ? '#f59e0b' : '#1b8a5a';

  const unreadCount = (data?.notifications || []).filter((n) => !n.isRead).length;
  const unpaidBills = (data?.bills || []).filter((b) => !b.isPaid);

  const toggleBalanceVisibility = async () => {
    const next = !isHidden;
    setHideBalances(next);
    await fetch('/api/profile', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ privacyHideBalances: next }),
    }).catch(() => {});
  };

  const markAllRead = async () => {
    await fetch('/api/notifications', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ markAllRead: true }),
    });
    refresh();
  };

  return (
    <NumoShell>
      {/* 1. Header Greeting + Notifications + Profile */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14, marginTop: 4 }}>
        <div>
          <div style={{ fontSize: 12, fontWeight: 700, color: '#00796b' }}>Numo</div>
          <h1 style={{ fontSize: 20, fontWeight: 800, color: '#2d1f14' }}>{greeting}</h1>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <button
            onClick={() => setShowNotifs(true)}
            aria-label="Notifications"
            className="numo-card"
            style={{
              width: 40,
              height: 40,
              borderRadius: 14,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              position: 'relative',
            }}
          >
            <Bell size={18} color="#2d1f14" />
            {(unreadCount > 0 || unpaidBills.length > 0) && (
              <span
                style={{
                  position: 'absolute',
                  top: 4,
                  right: 4,
                  width: 15,
                  height: 15,
                  borderRadius: '50%',
                  background: '#d81b60',
                  color: '#fff',
                  fontSize: 9,
                  fontWeight: 800,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                {unreadCount || unpaidBills.length}
              </span>
            )}
          </button>
          <Link
            href="/profile"
            aria-label="Profile"
            style={{
              width: 40,
              height: 40,
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #14b8a6, #00796b)',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 800,
              textDecoration: 'none',
              fontSize: 15,
              border: '2px solid #fff',
              boxShadow: '0 4px 12px rgba(0,121,107,0.2)',
            }}
          >
            {(data?.user?.firstName || data?.user?.fullName || 'N').charAt(0).toUpperCase()}
          </Link>
        </div>
      </div>

      {error && (
        <div className="numo-card" style={{ padding: 14, marginBottom: 14, borderColor: '#d81b60', color: '#d81b60', fontSize: 13 }}>
          <strong>Error:</strong> {error}
        </div>
      )}

      {/* 2. Available Cash Hero Card */}
      <div className="numo-hero-card" style={{ padding: '18px 20px', marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: 13, opacity: 0.92, fontWeight: 600 }}>Available Cash</span>
          <button
            onClick={toggleBalanceVisibility}
            aria-label="Toggle balance visibility"
            style={{ background: 'transparent', border: 'none', color: '#fff', cursor: 'pointer' }}
          >
            {isHidden ? <EyeOff size={20} /> : <Eye size={20} />}
          </button>
        </div>
        <div style={{ fontSize: 30, fontWeight: 800, marginTop: 6, letterSpacing: -0.5 }}>
          {loading ? 'KES ...' : showMoney(data?.availableCash ?? 0)}
        </div>
        <div style={{ fontSize: 12, opacity: 0.9, marginTop: 8 }}>
          Total: {showMoney(data?.totalAccountBalances ?? 0)} &bull; Reserved: {showMoney(data?.reservedAmount ?? 0)}
        </div>
      </div>

      {/* 3. Quick Actions */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10, marginBottom: 18 }}>
        <QuickActionTile href="/add-transaction?tab=expense" label="Add Expense" bg="linear-gradient(145deg,#4ade80,#15803d)" icon={<Plus size={20} />} />
        <QuickActionTile href="/add-transaction?tab=transfer" label="Transfer" bg="linear-gradient(145deg,#2dd4bf,#0d9488)" icon={<ArrowRightLeft size={19} />} />
        <QuickActionTile href="/add-transaction?tab=income" label="Receive Income" bg="linear-gradient(145deg,#fb923c,#dc2626)" icon={<Wallet size={19} />} />
        <QuickActionTile href="/upcoming-bills" label="Pay Bill" bg="linear-gradient(145deg,#38bdf8,#0284c7)" icon={<Receipt size={19} />} />
      </div>

      {/* 4. Monthly Cash Flow */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <h2 style={{ fontSize: 16, fontWeight: 800 }}>Monthly Cash Flow</h2>
        <Link href="/transactions" style={{ fontSize: 12, fontWeight: 700, color: '#5c4636', textDecoration: 'none' }}>
          View All &rsaquo;
        </Link>
      </div>
      <div className="numo-card" style={{ padding: 14, marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10 }}>
          <Link href="/transactions?type=INCOME" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: 8, color: 'inherit' }}>
            <div style={{ width: 32, height: 32, borderRadius: 10, background: 'rgba(27,138,90,0.14)', color: '#1b8a5a', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ArrowUpRight size={18} />
            </div>
            <div>
              <div style={{ fontSize: 14, fontWeight: 800, color: '#1b8a5a' }}>{showMoney(monthlyIncome)}</div>
              <div style={{ fontSize: 11, color: '#5c4636' }}>Income</div>
            </div>
          </Link>

          <Link href="/transactions?type=EXPENSE" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: 8, color: 'inherit', textAlign: 'right' }}>
            <div>
              <div style={{ fontSize: 14, fontWeight: 800, color: '#d81b60' }}>{showMoney(monthlySpent)}</div>
              <div style={{ fontSize: 11, color: '#5c4636' }}>Expenses</div>
            </div>
            <div style={{ width: 32, height: 32, borderRadius: 10, background: 'rgba(216,27,96,0.12)', color: '#d81b60', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ArrowDownLeft size={18} />
            </div>
          </Link>
        </div>

        <div style={{ height: 8, borderRadius: 999, background: 'rgba(216,27,96,0.75)', marginTop: 12, overflow: 'hidden' }}>
          <div style={{ width: `${incomePct}%`, height: '100%', background: '#1b8a5a', borderRadius: 999 }} />
        </div>

        <div style={{ textAlign: 'center', marginTop: 10, fontSize: 13, fontWeight: 800, color: netFlow >= 0 ? '#2d1f14' : '#d81b60' }}>
          Net: {showMoney(netFlow)}
        </div>
      </div>

      {/* 5. Budget Health & Net Worth Side-by-Side */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
        <Link href="/budget" className="numo-card" style={{ padding: 14, textDecoration: 'none', color: 'inherit', textAlign: 'center' }}>
          <div style={{ fontSize: 13, fontWeight: 800, marginBottom: 10 }}>Budget Health</div>
          <div
            style={{
              width: 72,
              height: 72,
              borderRadius: '50%',
              margin: '0 auto 8px',
              background: `conic-gradient(${budgetColor} ${Math.min(100, budgetPct) * 3.6}deg, #eadfcd 0deg)`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <div style={{ width: 54, height: 54, borderRadius: '50%', background: '#fdf8f0', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: 15 }}>
              {budgetPct}%
            </div>
          </div>
          <div style={{ fontSize: 13, fontWeight: 800, color: budgetColor }}>{budgetStatus}</div>
          <div style={{ fontSize: 11, color: '#5c4636' }}>{showMoney(data?.totalRemaining ?? 0)} left</div>
        </Link>

        <Link href="/net-worth" className="numo-card" style={{ padding: 14, textDecoration: 'none', color: 'inherit', textAlign: 'center', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div style={{ fontSize: 13, fontWeight: 800 }}>Net Worth</div>
          <div style={{ width: 52, height: 52, borderRadius: 16, margin: '10px auto', background: 'linear-gradient(145deg,#4ade80,#00796b)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <TrendingUp size={24} />
          </div>
          <div>
            <div style={{ fontSize: 16, fontWeight: 800 }}>{showMoney(data?.totalNetWorth ?? 0)}</div>
            <div style={{ fontSize: 11, color: '#5c4636' }}>Assets minus Liabilities</div>
          </div>
        </Link>
      </div>

      {/* 6. Connected Accounts Overview */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <h2 style={{ fontSize: 16, fontWeight: 800 }}>Connected Accounts</h2>
        <Link href="/accounts" style={{ fontSize: 12, fontWeight: 700, color: '#00796b', textDecoration: 'none' }}>
          Manage &rsaquo;
        </Link>
      </div>
      <div style={{ display: 'flex', gap: 10, overflowX: 'auto', paddingBottom: 6, marginBottom: 16 }}>
        {(data?.accounts || []).map((acc) => (
          <Link
            key={acc.id}
            href="/accounts"
            className="numo-card"
            style={{ minWidth: 140, padding: 12, textDecoration: 'none', color: 'inherit', flexShrink: 0 }}
          >
            <div style={{ fontSize: 11, color: '#5c4636', fontWeight: 600 }}>{acc.provider || acc.type}</div>
            <div style={{ fontSize: 14, fontWeight: 800, marginTop: 2 }}>{acc.name}</div>
            <div style={{ fontSize: 14, fontWeight: 800, color: '#00796b', marginTop: 6 }}>
              {showMoney(Number(acc.balance))}
            </div>
          </Link>
        ))}
      </div>

      {/* 7. Upcoming Bills Preview */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <h2 style={{ fontSize: 16, fontWeight: 800 }}>Upcoming Bills</h2>
        <Link href="/upcoming-bills" style={{ fontSize: 12, fontWeight: 700, color: '#5c4636', textDecoration: 'none' }}>
          See All &rsaquo;
        </Link>
      </div>
      <div style={{ display: 'grid', gap: 8, marginBottom: 16 }}>
        {unpaidBills.length === 0 ? (
          <div className="numo-card" style={{ padding: 14, fontSize: 13, color: '#5c4636' }}>
            No pending bills. Tap &ldquo;Pay Bill&rdquo; to schedule one.
          </div>
        ) : (
          unpaidBills.slice(0, 2).map((b) => (
            <Link
              key={b.id}
              href="/upcoming-bills"
              className="numo-card"
              style={{ padding: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', textDecoration: 'none', color: 'inherit' }}
            >
              <div>
                <div style={{ fontWeight: 800, fontSize: 14 }}>{b.title}</div>
                <div style={{ fontSize: 11, color: '#5c4636' }}>Due {formatLocalDate(b.dueDate)}</div>
              </div>
              <div style={{ fontWeight: 800, fontSize: 14, color: '#d81b60' }}>
                {showMoney(Math.max(0, Number(b.amount) - Number(b.paidAmount || 0)))}
              </div>
            </Link>
          ))
        )}
      </div>

      {/* 8. Recent Transactions */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <h2 style={{ fontSize: 16, fontWeight: 800 }}>Recent Transactions</h2>
        <Link href="/transactions" style={{ fontSize: 12, fontWeight: 700, color: '#5c4636', textDecoration: 'none' }}>
          History &rsaquo;
        </Link>
      </div>
      <div style={{ display: 'grid', gap: 8 }}>
        {(data?.transactions || []).length === 0 ? (
          <div className="numo-card" style={{ padding: 14, fontSize: 13, color: '#5c4636' }}>
            No transactions yet. Add your first expense, income, or transfer above.
          </div>
        ) : (
          (data?.transactions || []).slice(0, 4).map((tx) => (
            <Link
              key={tx.id}
              href="/transactions"
              className="numo-card"
              style={{ padding: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', textDecoration: 'none', color: 'inherit' }}
            >
              <div>
                <div style={{ fontWeight: 800, fontSize: 14 }}>{tx.description}</div>
                <div style={{ fontSize: 11, color: '#5c4636' }}>
                  {formatLocalDate(tx.date)} &bull; {tx.splits?.[0]?.category || tx.type}
                </div>
              </div>
              <div
                style={{
                  fontWeight: 800,
                  fontSize: 14,
                  color: tx.type === 'INCOME' ? '#1b8a5a' : tx.type === 'TRANSFER' ? '#00796b' : '#d81b60',
                }}
              >
                {tx.type === 'INCOME' ? '+' : tx.type === 'EXPENSE' ? '-' : ''}
                {showMoney(Number(tx.amount))}
              </div>
            </Link>
          ))
        )}
      </div>

      {/* Notifications Modal */}
      {showNotifs && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(11,11,11,0.45)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 18,
            zIndex: 60,
          }}
        >
          <div className="numo-card" style={{ width: '100%', maxWidth: 400, padding: 20, background: '#fef9ef' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <strong style={{ fontSize: 17 }}>Notifications</strong>
              <button onClick={() => setShowNotifs(false)} style={{ border: 'none', background: 'transparent', cursor: 'pointer', fontWeight: 800 }}>
                Close
              </button>
            </div>
            {(data?.notifications || []).length === 0 && unpaidBills.length === 0 ? (
              <p style={{ fontSize: 13, color: '#5c4636' }}>You are all caught up.</p>
            ) : (
              <div style={{ display: 'grid', gap: 8, maxHeight: 300, overflowY: 'auto' }}>
                {unpaidBills.map((b) => (
                  <div key={b.id} className="numo-card" style={{ padding: 10 }}>
                    <div style={{ fontWeight: 700, fontSize: 13 }}>Upcoming Bill: {b.title}</div>
                    <div style={{ fontSize: 12, color: '#5c4636' }}>
                      {formatKes(Number(b.amount) - Number(b.paidAmount || 0))} due {formatLocalDate(b.dueDate)}
                    </div>
                  </div>
                ))}
                {(data?.notifications || []).map((n) => (
                  <div key={n.id} className="numo-card" style={{ padding: 10 }}>
                    <div style={{ fontWeight: 700, fontSize: 13 }}>{n.title}</div>
                    <div style={{ fontSize: 12, color: '#5c4636' }}>{n.message}</div>
                  </div>
                ))}
              </div>
            )}
            <button onClick={markAllRead} className="numo-btn-primary" style={{ width: '100%', marginTop: 14 }}>
              Mark All Read
            </button>
          </div>
        </div>
      )}
    </NumoShell>
  );
}

function QuickActionTile({
  href,
  label,
  bg,
  icon,
}: {
  href: string;
  label: string;
  bg: string;
  icon: React.ReactNode;
}) {
  return (
    <Link
      href={href}
      style={{
        textDecoration: 'none',
        color: '#2d1f14',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 6,
      }}
    >
      <div
        style={{
          width: 52,
          height: 52,
          borderRadius: 16,
          background: bg,
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 8px 18px rgba(45, 31, 20, 0.16)',
        }}
      >
        {icon}
      </div>
      <span style={{ fontSize: 11, fontWeight: 700, textAlign: 'center', lineHeight: 1.2 }}>{label}</span>
    </Link>
  );
}
