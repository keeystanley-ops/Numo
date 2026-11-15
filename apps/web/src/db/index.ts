import { neon, Pool } from '@neondatabase/serverless';
import { drizzle } from 'drizzle-orm/neon-http';
import { drizzle as drizzleWs } from 'drizzle-orm/neon-serverless';
import * as schema from './schema';

export function isDatabaseConfigured(): boolean {
  const url = (process.env.DATABASE_URL || '').trim();
  return Boolean(url && !url.includes('ep-example'));
}

function getConnectionString(): string {
  const url = (process.env.DATABASE_URL || '').trim();
  if (!url) {
    throw new Error('DATABASE_URL is not set in environment variables');
  }
  return url;
}

export function getDb() {
  const sql = neon(getConnectionString());
  return drizzle(sql, { schema });
}

export function getPoolDb() {
  const pool = new Pool({ connectionString: getConnectionString() });
  return drizzleWs(pool, { schema });
}

export * from './schema';
