import { NextResponse } from 'next/server';
import crypto from 'node:crypto';
import { getRequestUserId } from '@/lib/auth';

export async function GET(req: Request) {
  const userId = await getRequestUserId(req);
  if (!userId) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const privateKey = (process.env.IMAGEKIT_PRIVATE_KEY || '').trim();
  const publicKey = (process.env.IMAGEKIT_PUBLIC_KEY || '').trim();
  const urlEndpoint = (process.env.IMAGEKIT_URL_ENDPOINT || '').trim();

  if (!privateKey || privateKey.includes('placeholder')) {
    return NextResponse.json(
      { error: 'IMAGEKIT_PRIVATE_KEY is not configured.' },
      { status: 503 }
    );
  }

  const token = crypto.randomUUID();
  const expire = Math.floor(Date.now() / 1000) + 1800; // 30 minutes
  const signature = crypto
    .createHmac('sha1', privateKey)
    .update(token + expire)
    .digest('hex');

  return NextResponse.json({
    token,
    expire,
    signature,
    publicKey,
    urlEndpoint,
  });
}
