import { NextResponse } from 'next/server';
import { and, desc, eq, inArray } from 'drizzle-orm';
import {
  getDb,
  holdings,
  investmentActivities,
  isDatabaseConfigured,
  valuationSnapshots,
} from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db.select().from(holdings).where(eq(holdings.userId, userId));
  const ids = rows.map((r) => r.id);
  const [activities, snapshots] =
    ids.length > 0
      ? await Promise.all([
          db
            .select()
            .from(investmentActivities)
            .where(inArray(investmentActivities.holdingId, ids))
            .orderBy(desc(investmentActivities.date))
            .limit(50),
          db
            .select()
            .from(valuationSnapshots)
            .where(inArray(valuationSnapshots.holdingId, ids))
            .orderBy(desc(valuationSnapshots.timestamp))
            .limit(50),
        ])
      : [[], []];

  return NextResponse.json({ holdings: rows, activities, snapshots });
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

  const units = Number(body.units || 1);
  const currentPrice = Number(body.currentPrice || body.purchasePrice || 0);

  const [created] = await db
    .insert(holdings)
    .values({
      userId,
      assetClass: body.assetClass || 'MMF',
      name: String(body.name || 'Money Market Fund'),
      ticker: body.ticker || null,
      units: units.toFixed(4),
      purchasePrice: Number(body.purchasePrice || currentPrice).toFixed(2),
      currentPrice: currentPrice.toFixed(2),
      currency: 'KES',
      fxRateToPrimary: '1.0000',
      dividendYield: body.dividendYield ? Number(body.dividendYield).toFixed(2) : null,
      dividendFrequency: body.dividendFrequency || 'ANNUAL',
    })
    .returning();

  await Promise.all([
    db.insert(valuationSnapshots).values({
      holdingId: created.id,
      valuation: (units * currentPrice).toFixed(2),
      notes: 'Initial holding valuation',
    }),
    db.insert(investmentActivities).values({
      holdingId: created.id,
      type: 'BUY',
      units: units.toFixed(4),
      amount: (units * currentPrice).toFixed(2),
      date: new Date(),
      notes: 'Initial investment',
    }),
  ]);

  return NextResponse.json({ holding: created }, { status: 201 });
}

export async function PATCH(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const body = await req.json();
  if (!body.id) return NextResponse.json({ error: 'Holding id required' }, { status: 400 });
  const db = getDb();

  const [existing] = await db
    .select()
    .from(holdings)
    .where(and(eq(holdings.id, body.id), eq(holdings.userId, userId)));
  if (!existing) return NextResponse.json({ error: 'Holding not found' }, { status: 404 });

  if (body.activityType) {
    await db.insert(investmentActivities).values({
      holdingId: existing.id,
      type: body.activityType,
      units: Number(body.units || 0).toFixed(4),
      amount: Number(body.amount || 0).toFixed(2),
      fees: Number(body.fees || 0).toFixed(2),
      withholdingTax: Number(body.withholdingTax || 0).toFixed(2),
      date: new Date(),
      notes: body.notes ? String(body.notes) : null,
    });
  }

  const newPrice = body.currentPrice !== undefined ? Number(body.currentPrice) : Number(existing.currentPrice);
  const newUnits = body.units !== undefined ? Number(body.units) : Number(existing.units);

  const [updated] = await db
    .update(holdings)
    .set({
      name: body.name ? String(body.name) : existing.name,
      currentPrice: newPrice.toFixed(2),
      units: newUnits.toFixed(4),
      lastValuationDate: new Date(),
    })
    .where(and(eq(holdings.id, existing.id), eq(holdings.userId, userId)))
    .returning();

  await db.insert(valuationSnapshots).values({
    holdingId: existing.id,
    valuation: (newPrice * newUnits).toFixed(2),
    notes: body.notes ? String(body.notes) : 'Valuation updated',
  });

  return NextResponse.json({ holding: updated });
}

export async function DELETE(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const { searchParams } = new URL(req.url);
  const id = searchParams.get('id');
  if (!id) return NextResponse.json({ error: 'Holding id required' }, { status: 400 });
  const db = getDb();
  await db.delete(holdings).where(and(eq(holdings.id, id), eq(holdings.userId, userId)));
  return NextResponse.json({ deletedId: id });
}
