import { NextResponse } from 'next/server';
import { isDatabaseConfigured } from '@/db';
import { isClerkConfigured } from '@/lib/auth';

export async function GET() {
  const geminiConfigured = Boolean(
    process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY'
  );
  const imageKitConfigured = Boolean(
    process.env.IMAGEKIT_PRIVATE_KEY &&
      !process.env.IMAGEKIT_PRIVATE_KEY.includes('placeholder')
  );

  return NextResponse.json({
    ok: true,
    service: 'numo-nextjs-backend',
    integrations: {
      database: isDatabaseConfigured(),
      clerk: isClerkConfigured(),
      gemini: geminiConfigured,
      imagekit: imageKitConfigured,
    },
  });
}
