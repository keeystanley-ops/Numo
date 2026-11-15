import { NextResponse } from 'next/server';
import { eq } from 'drizzle-orm';
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
