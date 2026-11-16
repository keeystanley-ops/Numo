import { auth } from '@clerk/nextjs/server';
import { cookies } from 'next/headers';
import { and, eq } from 'drizzle-orm';
import { accounts, getDb, isDatabaseConfigured, users } from '@/db';

export function isClerkConfigured(): boolean {
  const secret = (process.env.CLERK_SECRET_KEY || '').trim();
  const pub = (process.env.NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY || '').trim();
  return Boolean(
    secret &&
      pub &&
      !secret.includes('placeholder') &&
      !pub.includes('placeholder')
  );
}

/**
 * Resolves the authenticated user ID using Clerk first, with a safe local-dev fallback
 * via `x-numo-user-id` only when Clerk keys are not yet configured.
 */
export async function getRequestUserId(req?: Request): Promise<string | null> {
  if (isClerkConfigured()) {
    try {
      const session = await auth();
      if (session?.userId) return session.userId;
    } catch {
      // Fall through if called outside Clerk middleware
    }
  }

  const devHeader = req?.headers.get('x-numo-user-id')?.trim();
  if (!isClerkConfigured() && devHeader) {
    return devHeader;
  }

  if (!isClerkConfigured()) {
    try {
      const cookieStore = await cookies();
      if (cookieStore.get('numo_signed_out')?.value === '1') {
        return null;
      }
      const cookieUser = cookieStore.get('numo_user_id')?.value?.trim();
      if (cookieUser) return cookieUser;
    } catch {
      // Outside Next.js request context
    }
    return 'demo_user_ayesha';
  }

  return null;
}

export async function ensureUser(
  userId: string,
  email = 'ayesha@numo.com',
  fullName = 'Ayesha Wanjiru'
) {
  if (!isDatabaseConfigured()) return null;
  const db = getDb();
  let userRow = await db.query.users.findFirst({
    where: eq(users.id, userId),
  });
  if (!userRow) {
    const firstName = fullName.trim().split(/\s+/)[0] || null;
    const [created] = await db
      .insert(users)
      .values({
        id: userId,
        email,
        fullName,
        firstName,
        primaryCurrency: 'KES',
        country: 'KE',
      })
      .onConflictDoNothing()
      .returning();
    userRow = created ?? (await db.query.users.findFirst({ where: eq(users.id, userId) }));
  }

  // Ensure empty-account fallback: at least one default Cash wallet exists
  const activeAccounts = await db
    .select()
    .from(accounts)
    .where(and(eq(accounts.userId, userId), eq(accounts.isArchived, false)));

  if (activeAccounts.length === 0) {
    await db.insert(accounts).values({
      userId,
      name: 'Cash',
      provider: 'Cash Wallet',
      type: 'CASH',
      currency: 'KES',
      balance: '0.00',
      isDefaultCash: true,
    });
  }

  return userRow ?? null;
}
