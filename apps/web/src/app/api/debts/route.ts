import { NextResponse } from 'next/server';
import { eq } from 'drizzle-orm';
import { debts, getDb, isDatabaseConfigured } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';
import { calculateDebtStrategy } from '@/lib/accounting';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db.select().from(debts).where(eq(debts.userId, userId));
  const strategy = calculateDebtStrategy(rows, 0);
  return NextResponse.json({ debts: rows, strategy });
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
    .insert(debts)
    .values({
      userId,
      name: String(body.name || 'Loan'),
      originalAmount: Number(body.originalAmount || body.currentBalance || 0).toFixed(2),
      currentBalance: Number(body.currentBalance || 0).toFixed(2),
      interestRate: Number(body.interestRate || 12).toFixed(2),
      monthlyPayment: Number(body.monthlyPayment || 0).toFixed(2),
      dueDate: Number(body.dueDate || 25),
      lender: body.lender || null,
    })
    .returning();

  return NextResponse.json({ debt: created }, { status: 201 });
}
