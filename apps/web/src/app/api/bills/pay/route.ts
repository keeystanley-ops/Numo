import { NextResponse } from 'next/server';
import { isDatabaseConfigured } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';
import { payBillAtomic } from '@/lib/accounting';

export async function POST(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  try {
    const body = await req.json();
    await ensureUser(userId);
    const splits = Array.isArray(body.splits)
      ? body.splits.map((s: { accountId: string; amount: number }) => ({
          accountId: String(s.accountId),
          amount: Number(s.amount),
        }))
      : [{ accountId: String(body.accountId), amount: Number(body.amount) }];

    const result = await payBillAtomic({
      userId,
      billId: String(body.billId),
      splits,
      fee: body.fee ? Number(body.fee) : 0,
      receiptUrl: body.receiptUrl ? String(body.receiptUrl) : undefined,
      notes: body.notes ? String(body.notes) : undefined,
      idempotencyKey: body.idempotencyKey ? String(body.idempotencyKey) : undefined,
    });

    return NextResponse.json(result, { status: 201 });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to pay bill' },
      { status: 400 }
    );
  }
}
