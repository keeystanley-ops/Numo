import { NextResponse } from 'next/server';
import { runHeyNumoTts } from '@/lib/gemini';

export async function POST(req: Request) {
  const body = await req.json().catch(() => ({}));
  if (!body.text || !String(body.text).trim()) {
    return NextResponse.json({ error: 'Text is required.' }, { status: 400 });
  }
  const result = await runHeyNumoTts({
    text: String(body.text),
    voice: body.voice,
    style: body.style,
  });
  return NextResponse.json(result.body, { status: result.status });
}
