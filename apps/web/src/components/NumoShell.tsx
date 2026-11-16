'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  BarChart3,
  Home,
  PieChart,
  Target,
  User,
  Mic,
  TrendingUp,
  Landmark,
} from 'lucide-react';

interface NumoShellProps {
  children: React.ReactNode;
  title?: string;
  backHref?: string;
  rightSlot?: React.ReactNode;
  hideBottomNav?: boolean;
}

export function NumoShell({
  children,
  title,
  backHref,
  rightSlot,
  hideBottomNav = false,
}: NumoShellProps) {
  const pathname = usePathname();

  const navItems = [
    { href: '/dashboard', label: 'Home', icon: Home, match: ['/dashboard', '/upcoming-bills', '/add-transaction', '/transactions'] },
    { href: '/analytics', label: 'Analytics', icon: BarChart3, match: ['/analytics'] },
    { href: '/budget', label: 'Budget', icon: PieChart, match: ['/budget'] },
    { href: '/goals', label: 'Goals', icon: Target, match: ['/goals'] },
    { href: '/profile', label: 'Profile', icon: User, match: ['/profile', '/settings', '/accounts'] },
  ];

  return (
    <div style={{ minHeight: '100vh', display: 'flex', justifyContent: 'center' }}>
      <div
        style={{
          width: '100%',
          maxWidth: 480,
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          position: 'relative',
          paddingBottom: hideBottomNav ? 24 : 92,
        }}
      >
        {(title || backHref || rightSlot) && (
          <header
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '14px 18px 8px',
              position: 'sticky',
              top: 0,
              zIndex: 20,
              background: 'rgba(254, 249, 239, 0.88)',
              backdropFilter: 'blur(10px)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              {backHref && (
                <Link
                  href={backHref}
                  aria-label="Go Back"
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: 12,
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'rgba(255,255,255,0.85)',
                    color: '#2d1f14',
                    textDecoration: 'none',
                    fontWeight: 800,
                    boxShadow: '0 4px 10px rgba(91,58,41,0.08)',
                  }}
                >
                  &larr;
                </Link>
              )}
              {title && (
                <h1 style={{ fontSize: 18, fontWeight: 800, color: '#2d1f14' }}>{title}</h1>
              )}
            </div>
            <div>{rightSlot}</div>
          </header>
        )}

        {/* Environment Quick Strip on Main Tabs */}
        {!hideBottomNav && ['/dashboard', '/investments', '/net-worth'].includes(pathname || '') && (
          <div style={{ padding: '6px 18px 2px', display: 'flex', gap: 8 }}>
            <Link
              href="/dashboard"
              className="numo-pill"
              style={{
                textDecoration: 'none',
                background: pathname === '/dashboard' ? '#00796b' : '#f1e5cf',
                color: pathname === '/dashboard' ? '#fff' : '#5b3a29',
              }}
            >
              Money
            </Link>
            <Link
              href="/investments"
              className="numo-pill"
              style={{
                textDecoration: 'none',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 4,
                background: pathname === '/investments' ? '#00796b' : '#f1e5cf',
                color: pathname === '/investments' ? '#fff' : '#5b3a29',
              }}
            >
              <TrendingUp size={13} /> Investments
            </Link>
            <Link
              href="/net-worth"
              className="numo-pill"
              style={{
                textDecoration: 'none',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 4,
                background: pathname === '/net-worth' ? '#00796b' : '#f1e5cf',
                color: pathname === '/net-worth' ? '#fff' : '#5b3a29',
              }}
            >
              <Landmark size={13} /> Net Worth
            </Link>
            <Link
              href="/assistant"
              className="numo-pill"
              style={{
                marginLeft: 'auto',
                textDecoration: 'none',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 4,
                background: 'linear-gradient(135deg, #14b8a6, #00796b)',
                color: '#fff',
              }}
            >
              <Mic size={12} /> Hey Numo
            </Link>
          </div>
        )}

        <main style={{ flex: 1, padding: '10px 18px' }}>{children}</main>

        {!hideBottomNav && (
          <nav
            aria-label="Primary Navigation"
            style={{
              position: 'fixed',
              bottom: 0,
              left: '50%',
              transform: 'translateX(-50%)',
              width: '100%',
              maxWidth: 480,
              background: 'rgba(255, 251, 244, 0.96)',
              backdropFilter: 'blur(14px)',
              borderTop: '1px solid rgba(91, 58, 41, 0.1)',
              display: 'flex',
              justifyContent: 'space-around',
              padding: '8px 6px 12px',
              zIndex: 30,
              boxShadow: '0 -6px 20px rgba(45, 31, 20, 0.06)',
            }}
          >
            {navItems.map((item) => {
              const active = item.match.some((m) => pathname?.startsWith(m));
              const Icon = item.icon;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: 3,
                    textDecoration: 'none',
                    color: active ? '#00796b' : '#5c4636',
                    fontWeight: active ? 800 : 600,
                    fontSize: 11,
                    padding: '4px 10px',
                    borderRadius: 12,
                    background: active ? 'rgba(20, 184, 166, 0.12)' : 'transparent',
                  }}
                >
                  <Icon size={20} strokeWidth={active ? 2.4 : 1.9} />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>
        )}
      </div>
    </div>
  );
}
