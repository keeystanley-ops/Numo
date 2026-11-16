import { NextResponse } from 'next/server';
import { auth, clerkClient } from '@clerk/nextjs/server';
import { isClerkConfigured } from '@/lib/auth';

export async function POST() {
  if (isClerkConfigured()) {
    try {
      const session = await auth();
      if (session?.sessionId) {
        const client = await clerkClient();
        await client.sessions.revokeSession(session.sessionId);
      }
    } catch {
      // Continue clearing cookies even if Clerk session was already expired
    }
  }

  const res = NextResponse.json({ ok: true, signedOut: true });
  res.cookies.set('numo_signed_out', '1', {
    httpOnly: true,
    sameSite: 'lax',
    path: '/',
    maxAge: 60 * 60 * 24 * 7,
  });
  res.cookies.delete('numo_user_id');
  return res;
}
