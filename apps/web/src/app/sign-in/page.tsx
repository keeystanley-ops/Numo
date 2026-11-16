'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Eye, EyeOff, Lock, Mail } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';

export default function SignInPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!email.includes('@')) {
      setError('Enter a valid email address.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/profile');
      const json = await res.json();
      if (json?.user?.onboardingCompleted) {
        router.push('/dashboard');
      } else {
        router.push('/onboarding');
      }
    } catch {
      setError('Unable to verify session. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <NumoShell title="Sign In" backHref="/" hideBottomNav>
      <div className="numo-card" style={{ padding: 22, marginTop: 12 }}>
        <h2 style={{ fontSize: 22, fontWeight: 800 }}>Welcome back</h2>
        <p style={{ fontSize: 13, color: '#5c4636', marginTop: 4, marginBottom: 18 }}>
          Sign in to access your KES accounts, budgets, and ledger.
        </p>

        <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 14 }}>
          <div>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Email</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: 12, top: 14, color: '#5c4636' }} />
              <input
                type="email"
                className="numo-input"
                style={{ paddingLeft: 36 }}
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: 12, top: 14, color: '#5c4636' }} />
              <input
                type={showPw ? 'text' : 'password'}
                className="numo-input"
                style={{ paddingLeft: 36, paddingRight: 40 }}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button
                type="button"
                onClick={() => setShowPw(!showPw)}
                aria-label="Toggle password visibility"
                style={{
                  position: 'absolute',
                  right: 10,
                  top: 10,
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  color: '#5c4636',
                }}
              >
                {showPw ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <div style={{ textAlign: 'right' }}>
            <Link href="/forgot-password" style={{ fontSize: 12, fontWeight: 700, color: '#00796b', textDecoration: 'none' }}>
              Forgot Password?
            </Link>
          </div>

          {error && (
            <div style={{ padding: 10, borderRadius: 12, background: 'rgba(216,27,96,0.1)', color: '#d81b60', fontSize: 12, fontWeight: 700 }}>
              {error}
            </div>
          )}

          <button type="submit" disabled={loading} className="numo-btn-primary">
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: 13, marginTop: 18, color: '#5c4636' }}>
          New to Numo?{' '}
          <Link href="/sign-up" style={{ color: '#00796b', fontWeight: 800, textDecoration: 'none' }}>
            Create Account
          </Link>
        </p>
      </div>
    </NumoShell>
  );
}
