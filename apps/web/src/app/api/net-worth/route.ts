import { NextResponse } from 'next/server';
import { and, desc, eq } from 'drizzle-orm';
import {
  assets,
  getDb,
  isDatabaseConfigured,
  liabilities,
  netWorthSnapshots,
} from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';
import { getDashboardOverview } from '@/lib/accounting';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const [overview, snapshots] = await Promise.all([
    getDashboardOverview(userId),
    db
      .select()
      .from(netWorthSnapshots)
      .where(eq(netWorthSnapshots.userId, userId))
      .orderBy(desc(netWorthSnapshots.recordedAt))
      .limit(24),
  ]);

  return NextResponse.json({
    totalAccountBalances: overview.totalAccountBalances,
    totalPortfolioValuation: overview.totalPortfolioValuation,
    totalNonAccountAssets: overview.totalNonAccountAssets,
    totalAssets: overview.totalAssets,
    totalDebt: overview.totalDebt,
    totalOtherLiabilities: overview.totalOtherLiabilities,
    totalLiabilities: overview.totalLiabilities,
    totalNetWorth: overview.totalNetWorth,
    assets: overview.assets,
    liabilities: overview.liabilities,
    debts: overview.debts,
    snapshots,
  });
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

    if (body.kind === 'LIABILITY') {
      const [created] = await db
        .insert(liabilities)
        .values({
          userId,
          name: String(body.name || 'Liability').trim(),
          category: String(body.category || 'Loan'),
          currentBalance: Number(body.currentBalance || 0).toFixed(2),
          interestRate: Number(body.interestRate || 0).toFixed(2),
          notes: body.notes ? String(body.notes) : null,
        })
        .returning();
      return NextResponse.json({ liability: created }, { status: 201 });
    }

    const [created] = await db
      .insert(assets)
      .values({
        userId,
        name: String(body.name || 'Asset').trim(),
        category: String(body.category || 'Property'),
        currentValue: Number(body.currentValue || 0).toFixed(2),
        notes: body.notes ? String(body.notes) : null,
      })
      .returning();

    // Record snapshot after mutation
    const overview = await getDashboardOverview(userId);
    await db.insert(netWorthSnapshots).values({
      userId,
      totalAssets: overview.totalAssets.toFixed(2),
      totalLiabilities: overview.totalLiabilities.toFixed(2),
      netWorth: overview.totalNetWorth.toFixed(2),
    });

    return NextResponse.json({ asset: created }, { status: 201 });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to save net worth item' },
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
  const kind = searchParams.get('kind') || 'ASSET';
  if (!id) return NextResponse.json({ error: 'id required' }, { status: 400 });

  const db = getDb();
  if (kind === 'LIABILITY') {
    await db.delete(liabilities).where(and(eq(liabilities.id, id), eq(liabilities.userId, userId)));
  } else {
    await db.delete(assets).where(and(eq(assets.id, id), eq(assets.userId, userId)));
  }
  return NextResponse.json({ deletedId: id });
}
