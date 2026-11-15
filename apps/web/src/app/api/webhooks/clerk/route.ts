import { NextResponse } from 'next/server';
import { Webhook } from 'svix';
import { eq } from 'drizzle-orm';
import { getDb, isDatabaseConfigured, users } from '@/db';

export async function POST(req: Request) {
  const secret = (process.env.CLERK_WEBHOOK_SECRET || '').trim();
  if (!secret || secret.includes('placeholder')) {
    return NextResponse.json(
      { error: 'CLERK_WEBHOOK_SECRET is not configured.' },
      { status: 503 }
    );
  }

  const svixId = req.headers.get('svix-id');
  const svixTimestamp = req.headers.get('svix-timestamp');
  const svixSignature = req.headers.get('svix-signature');
  if (!svixId || !svixTimestamp || !svixSignature) {
    return NextResponse.json({ error: 'Missing Svix webhook headers.' }, { status: 400 });
  }

  const payload = await req.text();
  let evt: {
    type: string;
    data: {
      id: string;
      email_addresses?: Array<{ email_address: string }>;
      first_name?: string;
      last_name?: string;
    };
  };

  try {
    const wh = new Webhook(secret);
    evt = wh.verify(payload, {
      'svix-id': svixId,
      'svix-timestamp': svixTimestamp,
      'svix-signature': svixSignature,
    }) as typeof evt;
  } catch {
    return NextResponse.json({ error: 'Invalid webhook signature.' }, { status: 400 });
  }

  if (!isDatabaseConfigured()) {
    return NextResponse.json({ ok: true, skippedDb: true });
  }

  const db = getDb();
  const userId = evt.data.id;
  const email = evt.data.email_addresses?.[0]?.email_address || `${userId}@numo.app`;
  const fullName = [evt.data.first_name, evt.data.last_name].filter(Boolean).join(' ') || null;

  if (evt.type === 'user.created' || evt.type === 'user.updated') {
    await db
      .insert(users)
      .values({ id: userId, email, fullName })
      .onConflictDoUpdate({
        target: users.id,
        set: { email, fullName, updatedAt: new Date() },
      });
  } else if (evt.type === 'user.deleted') {
    await db.delete(users).where(eq(users.id, userId));
  }

  return NextResponse.json({ ok: true, event: evt.type });
}
