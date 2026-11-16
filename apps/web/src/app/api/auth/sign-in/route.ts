import { NextResponse } from 'next/server';
import { ensureUser } from '@/lib/auth';

export async function POST(req: Request) {
  try {
    const body = await req.json().catch(() => ({}));
    const email = String(body.email || 'ayesha@numo.com').trim();
    const fullName = body.fullName ? String(body.fullName).trim() : undefined;
    const userId = 'demo_user_ayesha';

    const user = await ensureUser(userId, email, fullName || 'Ayesha Wanjiru');

    const res = NextResponse.json({ ok: true, user });
    res.cookies.delete('numo_signed_out');
    res.cookies.set('numo_user_id', userId, {
      httpOnly: true,
      sameSite: 'lax',
      path: '/',
      maxAge: 60 * 60 * 24 * 30,
    });
    return res;
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Sign in failed' },
      { status: 400 }
    );
  }
}
