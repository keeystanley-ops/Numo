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

  const body = await req.json();
  await ensureUser(userId);
  const db = getDb();
  const [created] = await db
    .insert(accounts)
    .values({
      userId,
      name: String(body.name || 'M-PESA Wallet'),
      type: body.type || 'MPESA',
      currency: body.currency || 'KES',
      balance: Number(body.balance || 0).toFixed(2),
    })
    .returning();

  return NextResponse.json({ account: created }, { status: 201 });
}
