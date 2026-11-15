import { NextResponse } from 'next/server';
import { eq } from 'drizzle-orm';
import { accounts, budgets, getDb, isDatabaseConfigured, savingsGoals, users } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function POST(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const body = await req.json();
  await ensureUser(userId, body.email, body.fullName);
  const db = getDb();

  const currency = body.primaryCurrency || 'KES';
  const country = body.country || 'KE';
  const preferredEnvironment = body.preferredEnvironment === 'INVESTMENTS' ? 'INVESTMENTS' : 'MONEY';

  await db
    .update(users)
    .set({
      primaryCurrency: currency,
      country,
      preferredEnvironment,
      onboardingCompleted: true,
      updatedAt: new Date(),
    })
    .where(eq(users.id, userId));

  if (Array.isArray(body.startingAccounts) && body.startingAccounts.length > 0) {
    await db.insert(accounts).values(
      body.startingAccounts.map((a: { name: string; type: 'CASH' | 'MPESA' | 'BANK' | 'SAVINGS' | 'INVESTMENT_CASH'; balance: number }) => ({
        userId,
        name: a.name,
        type: a.type,
        currency,
        balance: Number(a.balance || 0).toFixed(2),
      }))
    );
  }

  if (Array.isArray(body.startingBudgets) && body.startingBudgets.length > 0) {
    await db.insert(budgets).values(
      body.startingBudgets.map((b: { category: string; limitAmount: number }) => ({
        userId,
        category: b.category,
        limitAmount: Number(b.limitAmount || 0).toFixed(2),
      }))
    );
  }

  if (Array.isArray(body.startingGoals) && body.startingGoals.length > 0) {
    await db.insert(savingsGoals).values(
      body.startingGoals.map((g: { title: string; targetAmount: number; savedAmount?: number; runwayMonths?: number }) => ({
        userId,
        title: g.title,
        targetAmount: Number(g.targetAmount || 0).toFixed(2),
        savedAmount: Number(g.savedAmount || 0).toFixed(2),
        runwayMonths: g.runwayMonths ?? null,
      }))
    );
  }

  return NextResponse.json({ ok: true, onboardingCompleted: true });
}
