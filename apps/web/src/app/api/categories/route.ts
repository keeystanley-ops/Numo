import { NextResponse } from 'next/server';
import { and, eq } from 'drizzle-orm';
import { getDb, isDatabaseConfigured, userCategories } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db.select().from(userCategories).where(eq(userCategories.userId, userId));
  return NextResponse.json({ categories: rows });
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
    .insert(userCategories)
    .values({
      userId,
      name: String(body.name || 'Custom').trim(),
      type: body.type === 'INCOME' ? 'INCOME' : 'EXPENSE',
      iconName: String(body.iconName || 'Category'),
      colorHex: String(body.colorHex || '#14B8A6'),
    })
    .returning();

  return NextResponse.json({ category: created }, { status: 201 });
}

export async function DELETE(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const { searchParams } = new URL(req.url);
  const id = searchParams.get('id');
  if (!id) return NextResponse.json({ error: 'id required' }, { status: 400 });

  const db = getDb();
  await db
    .delete(userCategories)
    .where(and(eq(userCategories.id, id), eq(userCategories.userId, userId)));
  return NextResponse.json({ deletedId: id });
}
