import { NextResponse } from 'next/server';
import { and, eq } from 'drizzle-orm';
import { budgets, getDb, isDatabaseConfigured } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db.select().from(budgets).where(eq(budgets.userId, userId));
  return NextResponse.json({ budgets: rows });
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
  const category = String(body.category || 'Food & Dining');
  const limitAmount = Number(body.limitAmount || 0).toFixed(2);

  const existing = await db
    .select()
    .from(budgets)
    .where(and(eq(budgets.userId, userId), eq(budgets.category, category)));

  if (existing[0]) {
    const [updated] = await db
      .update(budgets)
      .set({ limitAmount })
      .where(eq(budgets.id, existing[0].id))
      .returning();
    return NextResponse.json({ budget: updated });
  }

  const [created] = await db
    .insert(budgets)
    .values({
      userId,
      category,
      limitAmount,
      period: body.period || 'MONTHLY',
      alertThreshold: Number(body.alertThreshold || 80),
    })
    .returning();

  return NextResponse.json({ budget: created }, { status: 201 });
}
