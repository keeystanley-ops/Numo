import Link from 'next/link';
import { isDatabaseConfigured } from '@/db';
import { isClerkConfigured } from '@/lib/auth';

export const dynamic = 'force-dynamic';

export default function HomePage() {
  const dbReady = isDatabaseConfigured();
  const clerkReady = isClerkConfigured();
  const geminiReady = Boolean(
    process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY'
  );
  const imagekitReady = Boolean(
    process.env.IMAGEKIT_PRIVATE_KEY &&
      !process.env.IMAGEKIT_PRIVATE_KEY.includes('placeholder')
  );

  const checks = [
    { label: 'Neon PostgreSQL + Drizzle ORM', ok: dbReady, env: 'DATABASE_URL' },
    { label: 'Clerk Cloud Authentication', ok: clerkReady, env: 'CLERK_SECRET_KEY' },
    { label: 'Google Gemini AI & 3.8 TTS', ok: geminiReady, env: 'GEMINI_API_KEY' },
    { label: 'ImageKit.io Signed Uploads', ok: imagekitReady, env: 'IMAGEKIT_PRIVATE_KEY' },
  ];

  const endpoints = [
    'GET /api/health',
    'GET /api/dashboard',
    'GET, POST /api/accounts',
    'GET, POST /api/transactions',
    'GET, POST /api/budgets',
    'GET, POST /api/goals',
    'GET, POST /api/debts',
    'GET, POST /api/investments',
    'POST /api/onboarding',
    'GET /api/imagekit/auth',
    'POST /api/webhooks/clerk',
    'POST /api/hey-numo/chat | /tts | /confirm',
    'ANY /api/trpc/[trpc]',
  ];

  return (
    <main style={{ maxWidth: 980, margin: '0 auto', padding: '40px 20px' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 32, flexWrap: 'wrap', gap: 16 }}>
        <div>
          <span style={{ fontSize: 12, fontWeight: 700, color: '#007764', letterSpacing: 1.2, textTransform: 'uppercase' }}>
            Numo Personal Finance OS
          </span>
          <h1 style={{ fontSize: 34, fontWeight: 800, marginTop: 4 }}>
            Next.js Backend &amp; Web Companion
          </h1>
          <p style={{ color: '#5c4636', marginTop: 6 }}>
            Double-entry Neon PostgreSQL ledger, tRPC &amp; REST APIs, Clerk auth, and Hey Numo Gemini AI.
          </p>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <Link href="/dashboard" className="numo-btn-primary" style={{ textDecoration: 'none' }}>
            Open Dashboard
          </Link>
          <Link
            href="/assistant"
            style={{
              textDecoration: 'none',
              padding: '12px 20px',
              borderRadius: 16,
              fontWeight: 700,
              color: '#007764',
              background: 'rgba(255,255,255,0.9)',
              border: '1.5px solid #cdebe4',
            }}
          >
            Hey Numo AI
          </Link>
        </div>
      </header>

      <section className="numo-card" style={{ padding: 24, marginBottom: 24 }}>
        <h2 style={{ fontSize: 18, fontWeight: 700, marginBottom: 14 }}>Environment &amp; Integration Readiness</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12 }}>
          {checks.map((c) => (
            <div
              key={c.label}
              style={{
                padding: 14,
                borderRadius: 16,
                background: c.ok ? 'rgba(22, 191, 161, 0.12)' : 'rgba(224, 145, 31, 0.12)',
                border: `1px solid ${c.ok ? 'rgba(0, 119, 100, 0.3)' : 'rgba(224, 145, 31, 0.35)'}`,
              }}
            >
              <div style={{ fontWeight: 700, fontSize: 14 }}>{c.label}</div>
              <div style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>
                {c.ok ? 'Configured & Ready' : `Set ${c.env} in .env.local`}
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="numo-card" style={{ padding: 24 }}>
        <h2 style={{ fontSize: 18, fontWeight: 700, marginBottom: 12 }}>Available Backend API Routes</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: 8 }}>
          {endpoints.map((ep) => (
            <code
              key={ep}
              style={{
                padding: '10px 12px',
                borderRadius: 12,
                background: '#faf4e8',
                fontSize: 13,
                color: '#2d1f14',
                border: '1px solid #ece0ca',
              }}
            >
              {ep}
            </code>
          ))}
        </div>
      </section>
    </main>
  );
}
