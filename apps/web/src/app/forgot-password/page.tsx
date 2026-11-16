'use client';

import { useState } from 'react';
import Link from 'next/link';
import { CheckCircle2 } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.includes('@')) {
      setError('Enter the email address associated with your account.');
      return;
    }
    setError(null);
    setSent(true);
  };

  return (
    <NumoShell title="Password Recovery" backHref="/sign-in" hideBottomNav>
      <div className="numo-card" style={{ padding: 22, marginTop: 12 }}>
        {!sent ? (
          <>
            <h2 style={{ fontSize: 20, fontWeight: 800 }}>Reset Password</h2>
            <p style={{ fontSize: 13, color: '#5c4636', marginTop: 4, marginBottom: 16 }}>
              Enter your email and we will send password reset instructions via Clerk.
            </p>
            <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 14 }}>
              <input
                type="email"
                className="numo-input"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              {error && <div style={{ color: '#d81b60', fontSize: 12, fontWeight: 700 }}>{error}</div>}
              <button type="submit" className="numo-btn-primary">
                Send Reset Link
              </button>
            </form>
          </>
        ) : (
          <div style={{ textAlign: 'center', padding: '16px 0' }}>
            <CheckCircle2 size={48} color="#1b8a5a" style={{ margin: '0 auto 12px' }} />
            <h2 style={{ fontSize: 20, fontWeight: 800 }}>Check Your Email</h2>
            <p style={{ fontSize: 13, color: '#5c4636', marginTop: 6, marginBottom: 18 }}>
              If an account exists for <strong>{email}</strong>, a password reset link has been sent.
            </p>
            <Link href="/sign-in" className="numo-btn-primary" style={{ textDecoration: 'none', display: 'inline-block' }}>
              Back to Sign In
            </Link>
          </div>
        )}
      </div>
    </NumoShell>
  );
}
