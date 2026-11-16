import { NextResponse } from 'next/server';
import { and, desc, eq } from 'drizzle-orm';
import { getDb, isDatabaseConfigured, notifications } from '@/db';
import { getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const db = getDb();
  const rows = await db
    .select()
    .from(notifications)
    .where(eq(notifications.userId, userId))
    .orderBy(desc(notifications.createdAt))
    .limit(50);

  return NextResponse.json({ notifications: rows });
}

export async function PATCH(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const body = await req.json();
  const db = getDb();
  if (body.markAllRead) {
    await db.update(notifications).set({ isRead: true }).where(eq(notifications.userId, userId));
  } else if (body.id) {
    await db
      .update(notifications)
      .set({ isRead: true })
      .where(and(eq(notifications.id, body.id), eq(notifications.userId, userId)));
  }
  return NextResponse.json({ ok: true });
}
