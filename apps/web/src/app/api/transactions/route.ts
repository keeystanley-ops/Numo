import { NextResponse } from 'next/server';
import { desc, eq } from 'drizzle-orm';
import { getDb, isDatabaseConfigured, transactions } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';
import { recordDoubleEntryTransaction } from '@/lib/accounting';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db
    .select()
    .from(transactions)
    .where(eq(transactions.userId, userId))
    .orderBy(desc(transactions.date))
    .limit(50);

  return NextResponse.json({ transactions: rows });
}

export async function POST(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  try {
    const body = await req.json();
    await ensureUser(userId);
    const created = await recordDoubleEntryTransaction({
      userId,
      type: body.type || 'EXPENSE',
      amount: Number(body.amount),
      currency: body.currency || 'KES',
      date: body.date ? new Date(body.date) : new Date(),
      description: String(body.description || 'Transaction'),
      merchant: body.merchant,
      receiptUrl: body.receiptUrl,
      sourceAccountId: body.sourceAccountId,
      destinationAccountId: body.destinationAccountId,
      debtId: body.debtId,
      principalAmount: body.principalAmount ? Number(body.principalAmount) : undefined,
      interestAmount: body.interestAmount ? Number(body.interestAmount) : undefined,
      splits: body.splits,
    });
    return NextResponse.json({ transaction: created }, { status: 201 });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to record transaction' },
      { status: 400 }
    );
  }
}
