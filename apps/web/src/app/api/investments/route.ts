import { NextResponse } from 'next/server';
import { eq } from 'drizzle-orm';
import { getDb, holdings, isDatabaseConfigured, valuationSnapshots } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db.select().from(holdings).where(eq(holdings.userId, userId));
  return NextResponse.json({ holdings: rows });
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
      currency: body.currency || 'KES',
      fxRateToPrimary: Number(body.fxRateToPrimary || 1).toFixed(4),
      dividendYield: body.dividendYield ? Number(body.dividendYield).toFixed(2) : null,
      dividendFrequency: body.dividendFrequency || 'ANNUAL',
    })
    .returning();

  await db.insert(valuationSnapshots).values({
    holdingId: created.id,
    valuation: (units * currentPrice).toFixed(2),
    notes: 'Initial holding valuation',
  });

  return NextResponse.json({ holding: created }, { status: 201 });
}
