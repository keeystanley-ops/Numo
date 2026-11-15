import { NextResponse } from 'next/server';
import { ensureUser, getRequestUserId } from '@/lib/auth';
import { runHeyNumoChat } from '@/lib/gemini';

export async function POST(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

  const body = await req.json().catch(() => ({}));
  if (!body.message || !String(body.message).trim()) {
    return NextResponse.json({ error: 'Message cannot be empty.' }, { status: 400 });
  }

  await ensureUser(userId).catch(() => null);
  const result = await runHeyNumoChat({
    userId,
    userName: body.userName,
    message: String(body.message),
    financialSnapshot: body.financialSnapshot,
    resetHistory: Boolean(body.resetHistory),
  });

  return NextResponse.json(result.body, { status: result.status });
}
