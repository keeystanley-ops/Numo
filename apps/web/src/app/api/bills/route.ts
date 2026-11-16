import { NextResponse } from 'next/server';
import { and, desc, eq } from 'drizzle-orm';
import { billPayments, bills, getDb, isDatabaseConfigured } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const [userBills, payments] = await Promise.all([
    db.select().from(bills).where(eq(bills.userId, userId)).orderBy(bills.dueDate),
    db.select().from(billPayments).where(eq(billPayments.userId, userId)).orderBy(desc(billPayments.paidAt)),
  ]);

  return NextResponse.json({ bills: userBills, payments });
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
    const [created] = await db
      .insert(bills)
      .values({
        userId,
        title: String(body.title || 'Bill').trim(),
        amount: Number(body.amount || 0).toFixed(2),
        paidAmount: '0.00',
        dueDate: body.dueDate ? new Date(body.dueDate) : new Date(),
        category: String(body.category || 'Bills & Utilities'),
        accountNumber: body.accountNumber ? String(body.accountNumber).trim() : null,
        recurrence: String(body.recurrence || 'MONTHLY'),
        reminderEnabled: body.reminderEnabled !== false,
        reminderLeadDays: Number(body.reminderLeadDays ?? 3),
        defaultAccountId: body.defaultAccountId || null,
        autoPayPreference: Boolean(body.autoPayPreference),
        isPaid: false,
      })
      .returning();

    return NextResponse.json({ bill: created }, { status: 201 });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to create bill' },
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
    if (!body.id) return NextResponse.json({ error: 'Bill id required' }, { status: 400 });
    const db = getDb();
    const updates: Record<string, unknown> = {};
    if (body.title !== undefined) updates.title = String(body.title).trim();
    if (body.amount !== undefined) updates.amount = Number(body.amount).toFixed(2);
    if (body.dueDate !== undefined) updates.dueDate = new Date(body.dueDate);
    if (body.category !== undefined) updates.category = String(body.category);
    if (body.accountNumber !== undefined) updates.accountNumber = String(body.accountNumber).trim();
    if (body.recurrence !== undefined) updates.recurrence = String(body.recurrence);
    if (body.reminderEnabled !== undefined) updates.reminderEnabled = Boolean(body.reminderEnabled);
    if (body.reminderLeadDays !== undefined) updates.reminderLeadDays = Number(body.reminderLeadDays);
    if (body.autoPayPreference !== undefined) updates.autoPayPreference = Boolean(body.autoPayPreference);

    const [updated] = await db
      .update(bills)
      .set(updates)
      .where(and(eq(bills.id, body.id), eq(bills.userId, userId)))
      .returning();

    if (!updated) return NextResponse.json({ error: 'Bill not found' }, { status: 404 });
    return NextResponse.json({ bill: updated });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to update bill' },
      { status: 400 }
    );
  }
}

/**
 * Deletes a bill WITHOUT deleting historical bill_payments records.
 */
export async function DELETE(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const { searchParams } = new URL(req.url);
  const id = searchParams.get('id');
  if (!id) return NextResponse.json({ error: 'Bill id required' }, { status: 400 });

  const db = getDb();
  const [deleted] = await db
    .delete(bills)
    .where(and(eq(bills.id, id), eq(bills.userId, userId)))
    .returning();

  if (!deleted) return NextResponse.json({ error: 'Bill not found' }, { status: 404 });
  return NextResponse.json({ deleted });
}
