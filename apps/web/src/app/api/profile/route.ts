import { NextResponse } from 'next/server';
import { eq } from 'drizzle-orm';
import { getDb, isDatabaseConfigured, users } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  const user = await ensureUser(userId);
  return NextResponse.json({ user });
}

export async function PATCH(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  if (!isDatabaseConfigured()) {
    return NextResponse.json({ error: 'Database not configured.' }, { status: 503 });
  }

  try {
    const body = await req.json();
    await ensureUser(userId);
    const db = getDb();

    const updates: Record<string, unknown> = {
      updatedAt: new Date(),
      primaryCurrency: 'KES', // Strictly enforce KES operating currency
    };

    if (body.fullName !== undefined) {
      const full = String(body.fullName).trim();
      updates.fullName = full;
      updates.firstName = full.split(/\s+/)[0] || null;
    }
    if (body.firstName !== undefined) updates.firstName = String(body.firstName).trim();
    if (body.phone !== undefined) updates.phone = String(body.phone).trim();
    if (body.biometricEnabled !== undefined) updates.biometricEnabled = Boolean(body.biometricEnabled);
    if (body.privacyHideBalances !== undefined) updates.privacyHideBalances = Boolean(body.privacyHideBalances);
    if (body.voiceSpeed !== undefined) updates.voiceSpeed = String(body.voiceSpeed);
    if (body.voiceName !== undefined) updates.voiceName = String(body.voiceName);
    if (body.language !== undefined) updates.language = String(body.language);
    if (body.reminderNotifications !== undefined) updates.reminderNotifications = Boolean(body.reminderNotifications);
    if (body.budgetAlertNotifications !== undefined) updates.budgetAlertNotifications = Boolean(body.budgetAlertNotifications);
    if (body.preferredEnvironment !== undefined) updates.preferredEnvironment = String(body.preferredEnvironment);
    if (body.onboardingCompleted !== undefined) updates.onboardingCompleted = Boolean(body.onboardingCompleted);

    const [updated] = await db.update(users).set(updates).where(eq(users.id, userId)).returning();
    return NextResponse.json({ user: updated });
  } catch (err) {
    return NextResponse.json(
      { error: err instanceof Error ? err.message : 'Failed to update profile' },
      { status: 400 }
    );
  }
}
