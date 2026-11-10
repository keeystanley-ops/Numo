import { neon, Pool } from '@neondatabase/serverless';
import { drizzle } from 'drizzle-orm/neon-http';
import { drizzle as drizzleWs } from 'drizzle-orm/neon-serverless';
import * as schema from './schema';

const getConnectionString = () => {
  const url = process.env.DATABASE_URL;
  if (!url) {
    throw new Error('DATABASE_URL is not set in environment variables');
  }
  return url;
};

// HTTP-based client (ideal for serverless queries, edge routes, and fast stateless operations)
export const getDb = () => {
  const sql = neon(getConnectionString());
  return drizzle(sql, { schema });
};

// WebSocket/Pool-based client (for interactive transactions or batch operations)
export const getPoolDb = () => {
  const pool = new Pool({ connectionString: getConnectionString() });
  return drizzleWs(pool, { schema });
};

// Default export db instance
export const db = getDb();

export * from './schema';
