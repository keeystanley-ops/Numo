import { auth } from '@clerk/nextjs/server';
import { eq } from 'drizzle-orm';
import { getDb, isDatabaseConfigured, users } from '@/db';

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
  const existing = await db.query.users.findFirst({
    where: eq(users.id, userId),
  });
  if (existing) return existing;

  const [created] = await db
    .insert(users)
    .values({
      id: userId,
      email,
      fullName,
      primaryCurrency: 'KES',
      country: 'KE',
    })
    .onConflictDoNothing()
    .returning();

  return created ?? null;
}
