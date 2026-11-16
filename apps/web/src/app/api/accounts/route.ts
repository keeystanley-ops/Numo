import { NextResponse } from 'next/server';
import { and, eq } from 'drizzle-orm';
import { accounts, getDb, isDatabaseConfigured } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  await ensureUser(userId);
  const db = getDb();
  const rows = await db
    .select()
    .from(accounts)
    .where(and(eq(accounts.userId, userId), eq(accounts.isArchived, false)));
  return NextResponse.json({ accounts: rows });
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
      .insert(accounts)
      .values({
        userId,
        name: String(body.name || 'M-PESA').trim(),
        provider: String(body.provider || body.name || 'Numo').trim(),
        accountNumber: body.accountNumber ? String(body.accountNumber).trim() : null,
        type: body.type || 'MPESA',
        currency: 'KES',
        balance: Number(body.balance || 0).toFixed(2),
        isDefaultCash: Boolean(body.isDefaultCash),
      })
      .returning();

    return NextResponse.json({ account: created }, { status: 201 });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to create account' },
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
    if (!body.id) return NextResponse.json({ error: 'Account id is required' }, { status: 400 });
    const db = getDb();
    const updates: Record<string, unknown> = {};
    if (body.name !== undefined) updates.name = String(body.name).trim();
    if (body.provider !== undefined) updates.provider = String(body.provider).trim();
    if (body.accountNumber !== undefined) updates.accountNumber = String(body.accountNumber).trim();
    if (body.balance !== undefined) updates.balance = Number(body.balance).toFixed(2);
    if (body.type !== undefined) updates.type = body.type;

    const [updated] = await db
      .update(accounts)
      .set(updates)
      .where(and(eq(accounts.id, body.id), eq(accounts.userId, userId)))
      .returning();

    if (!updated) return NextResponse.json({ error: 'Account not found' }, { status: 404 });
    return NextResponse.json({ account: updated });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to update account' },
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

  const { searchParams } = new URL(req.url);
  const id = searchParams.get('id');
  if (!id) return NextResponse.json({ error: 'Account id is required' }, { status: 400 });

  const db = getDb();
  const [archived] = await db
    .update(accounts)
    .set({ isArchived: true })
    .where(and(eq(accounts.id, id), eq(accounts.userId, userId)))
    .returning();

  if (!archived) return NextResponse.json({ error: 'Account not found' }, { status: 404 });
  return NextResponse.json({ archived });
}
