'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Eye, EyeOff } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';

export default function SignUpPage() {
  const router = useRouter();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!fullName.trim()) return setError('Enter your full name.');
    if (!email.includes('@')) return setError('Enter a valid email address.');
    if (password.length < 6) return setError('Password must be at least 6 characters.');

    setLoading(true);
    try {
      const res = await fetch('/api/profile', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName: fullName.trim() }),
      });
      if (!res.ok) throw new Error('Could not save profile.');
      router.push('/onboarding');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sign up failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <NumoShell title="Create Account" backHref="/" hideBottomNav>
      <div className="numo-card" style={{ padding: 22, marginTop: 12 }}>
        <h2 style={{ fontSize: 22, fontWeight: 800 }}>Join Numo</h2>
        <p style={{ fontSize: 13, color: '#5c4636', marginTop: 4, marginBottom: 18 }}>
          Start tracking your Kenyan Shilling (KES) finances in one place.
        </p>

        <form onSubmit={handleSignUp} style={{ display: 'grid', gap: 14 }}>
          <div>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Full Name</label>
            <input
              type="text"
              className="numo-input"
              placeholder="e.g. Stalon Kiprop"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
            />
          </div>

          <div>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Email</label>
            <input
              type="email"
              className="numo-input"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 6 }}>Password</label>
            <div style={{ position: 'relative' }}>
              <input
                type={showPw ? 'text' : 'password'}
                className="numo-input"
                style={{ paddingRight: 40 }}
                placeholder="Min 6 characters"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button
                type="button"
                onClick={() => setShowPw(!showPw)}
                style={{ position: 'absolute', right: 10, top: 10, background: 'transparent', border: 'none', cursor: 'pointer', color: '#5c4636' }}
              >
                {showPw ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          {error && (
            <div style={{ padding: 10, borderRadius: 12, background: 'rgba(216,27,96,0.1)', color: '#d81b60', fontSize: 12, fontWeight: 700 }}>
              {error}
            </div>
          )}

          <button type="submit" disabled={loading} className="numo-btn-primary">
            {loading ? 'Creating...' : 'Continue to Onboarding'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: 13, marginTop: 18, color: '#5c4636' }}>
          Already have an account?{' '}
          <Link href="/sign-in" style={{ color: '#00796b', fontWeight: 800, textDecoration: 'none' }}>
            Sign In
          </Link>
        </p>
      </div>
    </NumoShell>
  );
}
