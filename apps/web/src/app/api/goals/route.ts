import { NextResponse } from 'next/server';
import { and, eq, sql } from 'drizzle-orm';
import { getDb, isDatabaseConfigured, savingsGoals } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db.select().from(savingsGoals).where(eq(savingsGoals.userId, userId));
  return NextResponse.json({ goals: rows });
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
    .insert(savingsGoals)
    .values({
      userId,
      title: String(body.title || 'Emergency Fund'),
      targetAmount: Number(body.targetAmount || 100000).toFixed(2),
      savedAmount: Number(body.savedAmount || 0).toFixed(2),
      monthlyContribution: Number(body.monthlyContribution || 0).toFixed(2),
      runwayMonths: body.runwayMonths ? Number(body.runwayMonths) : null,
      iconType: body.iconType || 'Shield',
    })
    .returning();

  return NextResponse.json({ goal: created }, { status: 201 });
}

export async function PATCH(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const body = await req.json();
  if (!body.id) return NextResponse.json({ error: 'Goal id required' }, { status: 400 });
  const db = getDb();

  if (body.delta !== undefined) {
    const delta = Number(body.delta);
    const [updated] = await db
      .update(savingsGoals)
      .set({
        savedAmount: sql`GREATEST(0, ${savingsGoals.savedAmount} + ${delta.toFixed(2)}::numeric)`,
      })
      .where(and(eq(savingsGoals.id, body.id), eq(savingsGoals.userId, userId)))
      .returning();
    if (!updated) return NextResponse.json({ error: 'Goal not found' }, { status: 404 });
    return NextResponse.json({ goal: updated });
  }

  const updates: Record<string, unknown> = {};
  if (body.title !== undefined) updates.title = String(body.title);
  if (body.targetAmount !== undefined) updates.targetAmount = Number(body.targetAmount).toFixed(2);
  if (body.monthlyContribution !== undefined) updates.monthlyContribution = Number(body.monthlyContribution).toFixed(2);

  const [updated] = await db
    .update(savingsGoals)
    .set(updates)
    .where(and(eq(savingsGoals.id, body.id), eq(savingsGoals.userId, userId)))
    .returning();
  return NextResponse.json({ goal: updated });
}

export async function DELETE(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const { searchParams } = new URL(req.url);
  const id = searchParams.get('id');
  if (!id) return NextResponse.json({ error: 'Goal id required' }, { status: 400 });
  const db = getDb();
  await db.delete(savingsGoals).where(and(eq(savingsGoals.id, id), eq(savingsGoals.userId, userId)));
  return NextResponse.json({ deletedId: id });
}
