import { initTRPC, TRPCError } from '@trpc/server';
import superjson from 'superjson';
import { ensureUser, getRequestUserId } from '@/lib/auth';

export async function createTRPCContext(opts?: { req?: Request }) {
  const userId = await getRequestUserId(opts?.req);
  if (userId) {
    await ensureUser(userId).catch(() => null);
  }
  return { userId };
}

export type TRPCContext = Awaited<ReturnType<typeof createTRPCContext>>;

const t = initTRPC.context<TRPCContext>().create({
  transformer: superjson,
});

export const router = t.router;
export const publicProcedure = t.procedure;

export const protectedProcedure = t.procedure.use(({ ctx, next }) => {
  if (!ctx.userId) {
    throw new TRPCError({ code: 'UNAUTHORIZED', message: 'Authentication required' });
  }
  return next({ ctx: { userId: ctx.userId } });
});
