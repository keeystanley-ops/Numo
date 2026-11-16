import { NextResponse } from 'next/server';
import { and, desc, eq, inArray } from 'drizzle-orm';
import {
  getDb,
  isDatabaseConfigured,
  recurringSchedules,
  transactions,
  transactionSplits,
} from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';
import { deleteTransactionAtomic, recordDoubleEntryTransaction } from '@/lib/accounting';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const { searchParams } = new URL(req.url);
  const typeFilter = searchParams.get('type');
  const q = (searchParams.get('q') || '').trim().toLowerCase();
  const accountId = searchParams.get('accountId');
  const sort = searchParams.get('sort') || 'newest';

  const db = getDb();
  const rows = await db
    .select()
    .from(transactions)
    .where(eq(transactions.userId, userId))
    .orderBy(desc(transactions.date))
    .limit(200);

  const txIds = rows.map((r) => r.id);
  const splitsRows =
    txIds.length > 0
      ? await db.select().from(transactionSplits).where(inArray(transactionSplits.transactionId, txIds))
      : [];
  const splitsMap = new Map<string, typeof splitsRows>();
  for (const s of splitsRows) {
    const arr = splitsMap.get(s.transactionId) || [];
    arr.push(s);
    splitsMap.set(s.transactionId, arr);
  }

  let enriched = rows.map((r) => ({
    ...r,
    splits: splitsMap.get(r.id) || [],
  }));

  if (typeFilter && typeFilter !== 'ALL') {
    enriched = enriched.filter((t) => t.type === typeFilter.toUpperCase());
  }
  if (accountId && accountId !== 'ALL') {
    enriched = enriched.filter(
      (t) => t.sourceAccountId === accountId || t.destinationAccountId === accountId
    );
  }
  if (q) {
    enriched = enriched.filter((t) => {
      const cats = t.splits.map((s) => s.category).join(' ');
      const hay = `${t.description} ${t.merchant || ''} ${t.reference || ''} ${cats}`.toLowerCase();
      return hay.includes(q);
    });
  }

  if (sort === 'oldest') {
    enriched.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  } else if (sort === 'highest') {
    enriched.sort((a, b) => Number(b.amount) - Number(a.amount));
  } else if (sort === 'lowest') {
    enriched.sort((a, b) => Number(a.amount) - Number(b.amount));
  }

  return NextResponse.json({ transactions: enriched });
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
    const db = getDb();

    // Support Split Deposits for Income (credits each destination account atomically)
    if (body.type === 'INCOME' && Array.isArray(body.depositSplits) && body.depositSplits.length > 0) {
      const createdList = [];
      for (let i = 0; i < body.depositSplits.length; i++) {
        const dep = body.depositSplits[i];
        const created = await recordDoubleEntryTransaction({
          userId,
          type: 'INCOME',
          amount: Number(dep.amount),
          grossAmount: i === 0 ? Number(body.grossAmount ?? dep.amount) : Number(dep.amount),
          deductionAmount: i === 0 ? Number(body.deductionAmount ?? 0) : 0,
          currency: 'KES',
          date: body.date ? new Date(body.date) : new Date(),
          description:
            body.depositSplits.length > 1
              ? `${String(body.description || 'Income')} (Split)`
              : String(body.description || 'Income'),
          merchant: body.merchant,
          receiptUrl: body.receiptUrl,
          destinationAccountId: dep.accountId,
          splits: [{ category: body.category || 'Salary', amount: Number(dep.amount) }],
          idempotencyKey: body.idempotencyKey ? `${body.idempotencyKey}_split_${i}` : undefined,
        });
        createdList.push(created);
      }

      if (body.recurringFrequency && body.recurringFrequency !== 'ONE_TIME') {
        await db.insert(recurringSchedules).values({
          userId,
          title: String(body.description || body.category || 'Recurring Income'),
          type: 'INCOME',
          amount: Number(body.amount).toFixed(2),
          category: String(body.category || 'Salary'),
          destinationAccountId: body.depositSplits[0].accountId,
          frequency: String(body.recurringFrequency),
        });
      }

      return NextResponse.json({ transactions: createdList }, { status: 201 });
    }

    const created = await recordDoubleEntryTransaction({
      userId,
      type: body.type || 'EXPENSE',
      amount: Number(body.amount),
      grossAmount: body.grossAmount !== undefined ? Number(body.grossAmount) : undefined,
      deductionAmount: body.deductionAmount !== undefined ? Number(body.deductionAmount) : 0,
      fee: body.fee !== undefined ? Number(body.fee) : 0,
      currency: 'KES',
      date: body.date ? new Date(body.date) : new Date(),
      description: String(body.description || 'Transaction'),
      merchant: body.merchant,
      reference: body.reference,
      receiptUrl: body.receiptUrl,
      sourceAccountId: body.sourceAccountId,
      destinationAccountId: body.destinationAccountId,
      debtId: body.debtId,
      principalAmount: body.principalAmount ? Number(body.principalAmount) : undefined,
      interestAmount: body.interestAmount ? Number(body.interestAmount) : undefined,
      splits: body.splits,
      idempotencyKey: body.idempotencyKey,
      metadata: body.metadata,
    });

    if (body.recurringFrequency && body.recurringFrequency !== 'ONE_TIME') {
      await db.insert(recurringSchedules).values({
        userId,
        title: String(body.description || 'Recurring Transaction'),
        type: body.type || 'TRANSFER',
        amount: Number(body.amount).toFixed(2),
        fee: Number(body.fee || 0).toFixed(2),
        category: String(body.splits?.[0]?.category || body.category || 'Transfer'),
        sourceAccountId: body.sourceAccountId || null,
        destinationAccountId: body.destinationAccountId || null,
        frequency: String(body.recurringFrequency),
      });
    }

    return NextResponse.json({ transaction: created }, { status: 201 });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to record transaction' },
      { status: 400 }
    );
  }
}

export async function PATCH(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  try {
    const body = await req.json();
    if (!body.id) return NextResponse.json({ error: 'Transaction id required' }, { status: 400 });

    const db = getDb();
    const [existing] = await db
      .select()
      .from(transactions)
      .where(and(eq(transactions.id, body.id), eq(transactions.userId, userId)));
    if (!existing) return NextResponse.json({ error: 'Transaction not found' }, { status: 404 });

    // If amount changed, atomically reverse old and record updated
    if (body.amount !== undefined && Number(body.amount) !== Number(existing.amount)) {
      await deleteTransactionAtomic(userId, existing.id);
      const recreated = await recordDoubleEntryTransaction({
        userId,
        type: existing.type,
        amount: Number(body.amount),
        fee: Number(body.fee ?? existing.fee ?? 0),
        date: body.date ? new Date(body.date) : existing.date,
        description: String(body.description ?? existing.description),
        merchant: body.merchant ?? existing.merchant ?? undefined,
        reference: body.reference ?? existing.reference ?? undefined,
        sourceAccountId: body.sourceAccountId ?? existing.sourceAccountId ?? undefined,
        destinationAccountId: body.destinationAccountId ?? existing.destinationAccountId ?? undefined,
        splits: body.category
          ? [{ category: String(body.category), amount: Number(body.amount) }]
          : undefined,
      });
      return NextResponse.json({ transaction: recreated });
    }

    const [updated] = await db
      .update(transactions)
      .set({
        description: body.description !== undefined ? String(body.description) : existing.description,
        reference: body.reference !== undefined ? String(body.reference) : existing.reference,
        date: body.date ? new Date(body.date) : existing.date,
      })
      .where(and(eq(transactions.id, body.id), eq(transactions.userId, userId)))
      .returning();

    return NextResponse.json({ transaction: updated });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to update transaction' },
      { status: 400 }
    );
  }
}

export async function DELETE(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  try {
    const { searchParams } = new URL(req.url);
    const id = searchParams.get('id');
    if (!id) return NextResponse.json({ error: 'Transaction id required' }, { status: 400 });

    const result = await deleteTransactionAtomic(userId, id);
    return NextResponse.json(result);
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to delete transaction' },
      { status: 400 }
    );
  }
}
