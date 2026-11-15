import { NextResponse } from 'next/server';
import { isDatabaseConfigured } from '@/db';
import { ensureUser, getRequestUserId } from '@/lib/auth';
import { getDashboardOverview } from '@/lib/accounting';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }
  if (!isDatabaseConfigured()) {
    return NextResponse.json(
      { error: 'DATABASE_URL is not configured. Add your Neon connection string to .env.local.' },
      { status: 503 }
    );
  }

  await ensureUser(userId);
  const overview = await getDashboardOverview(userId);
  return NextResponse.json(overview);
}
