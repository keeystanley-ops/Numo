import { NextResponse } from 'next/server';
import { getRequestUserId } from '@/lib/auth';
import { confirmHeyNumoAction } from '@/lib/gemini';

export async function POST(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

  const body = await req.json().catch(() => ({}));
  const result = await confirmHeyNumoAction({
    userId,
    action: body.action || { amount: 0, description: 'Transaction', category: 'Other' },
    confirmed: Boolean(body.confirmed),
  });
  return NextResponse.json(result.body, { status: result.status });
}
