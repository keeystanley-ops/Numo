import { desc, eq } from 'drizzle-orm';
import {
  assistantMessages,
  assistantSessions,
  getDb,
  isDatabaseConfigured,
} from '@/db';
import {
  calculateDebtStrategy,
  getDashboardOverview,
  recordDoubleEntryTransaction,
} from '@/lib/accounting';

const GEMINI_MODEL = 'gemini-2.5-flash';
const GEMINI_ENDPOINT = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;
const GEMINI_TTS_MODEL = 'gemini-3.8-flash-lite-tts';
const GEMINI_INTERACTIONS_ENDPOINT = 'https://generativelanguage.googleapis.com/v1beta/interactions';

const ALLOWED_VOICES = new Set([
  'Sulafat',
  'Vindemiatrix',
  'Achernar',
  'Aoede',
  'Despina',
  'Achird',
]);

function getGeminiApiKey(): string | null {
  const key = (process.env.GEMINI_API_KEY || '').trim();
  if (!key || key === 'MY_GEMINI_API_KEY') return null;
  return key;
}

const GEMINI_TOOLS = [
  {
    functionDeclarations: [
      {
        name: 'getAccountBalances',
        description: 'Get linked bank, M-PESA, savings, and cash account balances.',
        parameters: { type: 'OBJECT', properties: {} },
      },
      {
        name: 'getAvailableCash',
        description: 'Get total liquid available cash balance and currency.',
        parameters: { type: 'OBJECT', properties: {} },
      },
      {
        name: 'getTransactions',
        description: 'Get recent transactions.',
        parameters: {
          type: 'OBJECT',
          properties: {
            limit: { type: 'INTEGER', description: 'Number of transactions to return' },
          },
        },
      },
      {
        name: 'getBudgetStatus',
        description: 'Get monthly budget limits and spending status.',
        parameters: {
          type: 'OBJECT',
          properties: {
            category: { type: 'STRING', description: 'Optional category name' },
          },
        },
      },
      {
        name: 'getUpcomingBills',
        description: 'Get unpaid upcoming bills.',
        parameters: { type: 'OBJECT', properties: {} },
      },
      {
        name: 'getSavingsGoals',
        description: 'Get savings goals and emergency runway progress.',
        parameters: { type: 'OBJECT', properties: {} },
      },
      {
        name: 'getDebtSummary',
        description: 'Get active debts and Avalanche vs Snowball payoff strategy.',
        parameters: { type: 'OBJECT', properties: {} },
      },
      {
        name: 'getInvestmentPortfolio',
        description: 'Get investment holdings, valuation, and total return.',
        parameters: { type: 'OBJECT', properties: {} },
      },
      {
        name: 'getNetWorth',
        description: 'Get total net worth combining cash, investments, and debts.',
        parameters: { type: 'OBJECT', properties: {} },
      },
      {
        name: 'createTransaction',
        description: 'Prepare an expense, income, or transfer confirmation card. Never saves without user confirmation.',
        parameters: {
          type: 'OBJECT',
          properties: {
            amount: { type: 'NUMBER' },
            description: { type: 'STRING' },
            account: { type: 'STRING' },
            category: { type: 'STRING' },
            isIncome: { type: 'BOOLEAN' },
          },
          required: ['amount', 'description', 'account', 'category', 'isIncome'],
        },
      },
    ],
  },
];

export async function runHeyNumoChat(params: {
  userId: string;
  userName?: string;
  message: string;
  financialSnapshot?: Record<string, unknown>;
  resetHistory?: boolean;
}) {
  const apiKey = getGeminiApiKey();
  if (!apiKey) {
    return {
      status: 503,
      body: {
        errorCode: 'GEMINI_KEY_MISSING',
        reply: 'Hey Numo AI is waiting for GEMINI_API_KEY to be configured in apps/web/.env.local.',
      },
    };
  }

  const useDb = isDatabaseConfigured();
  const dbSnapshot = useDb ? await getDashboardOverview(params.userId).catch(() => null) : null;
  const snapshot = dbSnapshot || params.financialSnapshot || {};
  const currency = (snapshot as { currency?: string }).currency || 'KES';
  const firstName = (params.userName || '').trim().split(/\s+/)[0] || '';

  // Load or create persistent session in Neon Postgres if DB is configured
  let sessionId: string | null = null;
  let historyContents: Array<{ role: string; parts: Array<{ text: string }> }> = [];

  if (useDb) {
    const db = getDb();
    if (!params.resetHistory) {
      const latest = await db
        .select()
        .from(assistantSessions)
        .where(eq(assistantSessions.userId, params.userId))
        .orderBy(desc(assistantSessions.updatedAt))
        .limit(1);
      if (latest[0]) {
        sessionId = latest[0].id;
        const past = await db
          .select()
          .from(assistantMessages)
          .where(eq(assistantMessages.sessionId, sessionId))
          .orderBy(desc(assistantMessages.createdAt))
          .limit(12);
        historyContents = past
          .reverse()
          .map((m) => ({ role: m.role, parts: [{ text: m.content }] }));
      }
    }
    if (!sessionId) {
      const [createdSession] = await db
        .insert(assistantSessions)
        .values({ userId: params.userId, title: params.message.slice(0, 48) })
        .returning();
      sessionId = createdSession.id;
    }
  }

  const systemInstruction = {
    parts: [
      {
        text: [
          'You are Numo ("Hey Numo"), a warm, calm, intelligent personal finance companion inside the Numo Personal Finance OS.',
          firstName ? `The user's first name is ${firstName}. Use it occasionally, not in every response.` : '',
          `Preferred currency: ${currency}. Pronounce KES, M-PESA, SACCO, and Kenyan financial terms naturally.`,
          'Never use robotic phrases like "Certainly!" or "As an AI language model...". Answer the question first in 1-3 warm, natural sentences.',
          'Always call the financial tools to retrieve real data; never invent figures. For transactions, call createTransaction so the app shows an Awaiting Approval confirmation card.',
        ]
          .filter(Boolean)
          .join('\n'),
      },
    ],
  };

  const workingContents: Array<Record<string, unknown>> = [
    ...historyContents,
    { role: 'user', parts: [{ text: params.message }] },
  ];

  let pendingAction: Record<string, unknown> | null = null;
  let replyText = '';

  for (let round = 0; round < 4; round++) {
    const res = await fetch(GEMINI_ENDPOINT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-goog-api-key': apiKey,
      },
      body: JSON.stringify({
        systemInstruction,
        contents: workingContents,
        tools: GEMINI_TOOLS,
      }),
    });

    const json = await res.json().catch(() => null);
    if (!res.ok || !json) {
      return {
        status: res.status || 502,
        body: {
          errorCode: 'GEMINI_API_ERROR',
          reply: 'Hey Numo could not reach Gemini right now. Please try again in a moment.',
        },
      };
    }

    const parts = json?.candidates?.[0]?.content?.parts || [];
    if (parts.length > 0) {
      workingContents.push({ role: 'model', parts });
    }

    const calls = parts.filter((p: { functionCall?: unknown }) => p.functionCall);
    if (calls.length === 0) {
      replyText = parts.map((p: { text?: string }) => p.text || '').join(' ').trim();
      break;
    }

    const responseParts: Array<Record<string, unknown>> = [];
    for (const part of calls) {
      const { name, args = {} } = part.functionCall;
      if (name === 'createTransaction') {
        pendingAction = {
          id: `act_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
          kind: 'TRANSACTION',
          amount: Number(args.amount || 0),
          description: String(args.description || 'Transaction'),
          account: String(args.account || 'M-PESA'),
          category: String(args.category || 'Other'),
          isIncome: Boolean(args.isIncome),
          currency,
          statusLabel: 'Awaiting Approval',
        };
        responseParts.push({
          functionResponse: {
            name,
            response: { status: 'AWAITING_USER_CONFIRMATION', preparedAction: pendingAction },
          },
        });
      } else {
        const s = snapshot as Record<string, unknown>;
        const toolResult =
          name === 'getAvailableCash'
            ? { currency, availableCash: s.availableCash ?? 0 }
            : name === 'getAccountBalances'
            ? { currency, accounts: s.accounts ?? [] }
            : name === 'getTransactions'
            ? { currency, transactions: s.transactions ?? [] }
            : name === 'getBudgetStatus'
            ? { currency, budgets: s.budgets ?? [], monthlySpent: s.monthlySpent ?? 0 }
            : name === 'getUpcomingBills'
            ? { currency, bills: s.bills ?? [] }
            : name === 'getSavingsGoals'
            ? { currency, goals: s.goals ?? [] }
            : name === 'getDebtSummary'
            ? {
                currency,
                debts: s.debts ?? [],
                strategy: calculateDebtStrategy(
                  (s.debts as Array<{ id: string; name: string; currentBalance: string; interestRate: string; monthlyPayment: string }>) || []
                ),
              }
            : name === 'getInvestmentPortfolio'
            ? {
                currency,
                totalPortfolioValuation: s.totalPortfolioValuation ?? 0,
                totalPortfolioReturn: s.totalPortfolioReturn ?? 0,
                holdings: s.holdings ?? [],
              }
            : name === 'getNetWorth'
            ? { currency, totalNetWorth: s.totalNetWorth ?? 0, totalDebt: s.totalDebt ?? 0 }
            : { currency };

        responseParts.push({
          functionResponse: { name, response: toolResult },
        });
      }
    }
    workingContents.push({ role: 'user', parts: responseParts });
  }

  if (!replyText) {
    replyText = pendingAction
      ? `Got it! I've prepared ${currency} ${pendingAction.amount} for ${pendingAction.description}. Tap Confirm when you're ready to save it.`
      : 'I checked your Numo records, but could not generate a clear reply.';
  }

  if (useDb && sessionId) {
    const db = getDb();
    await db.insert(assistantMessages).values([
      { sessionId, role: 'user', content: params.message },
      { sessionId, role: 'model', content: replyText, pendingAction },
    ]);
    await db
      .update(assistantSessions)
      .set({ updatedAt: new Date() })
      .where(eq(assistantSessions.id, sessionId));
  }

  return {
    status: 200,
    body: {
      reply: replyText,
      spokenText: replyText,
      pendingAction,
      sessionId,
    },
  };
}

export async function confirmHeyNumoAction(params: {
  userId: string;
  action: {
    amount: number;
    description: string;
    category: string;
    isIncome?: boolean;
  };
  confirmed: boolean;
}) {
  if (!params.confirmed) {
    return { status: 200, body: { status: 'CANCELLED' } };
  }
  if (isDatabaseConfigured()) {
    const created = await recordDoubleEntryTransaction({
      userId: params.userId,
      type: params.action.isIncome ? 'INCOME' : 'EXPENSE',
      amount: Number(params.action.amount),
      description: params.action.description,
      splits: [{ category: params.action.category || 'Other', amount: Number(params.action.amount) }],
    });
    return { status: 200, body: { status: 'CONFIRMED', transaction: created } };
  }
  return { status: 200, body: { status: 'CONFIRMED' } };
}

export async function runHeyNumoTts(params: {
  text: string;
  voice?: string;
  style?: string;
}) {
  const apiKey = getGeminiApiKey();
  if (!apiKey) {
    return { status: 503, body: { error: 'GEMINI_API_KEY is not configured.' } };
  }
  const voice = params.voice && ALLOWED_VOICES.has(params.voice) ? params.voice : 'Sulafat';
  const style = params.style || 'warm, calm, natural and conversational';

  const res = await fetch(GEMINI_INTERACTIONS_ENDPOINT, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-goog-api-key': apiKey,
    },
    body: JSON.stringify({
      model: GEMINI_TTS_MODEL,
      input: [
        {
          type: 'user_input',
          content: [
            {
              type: 'text',
              text: params.text,
              annotations: [{ type: 'speech_metadata', style }],
            },
          ],
        },
      ],
      response_format: { type: 'audio' },
      generation_config: { speech_config: [{ voice }] },
    }),
  });

  const json = await res.json().catch(() => null);
  if (!res.ok || !json) {
    return { status: res.status || 502, body: { error: 'Gemini TTS request failed.' } };
  }

  let audioBase64 = json?.output_audio?.data || null;
  if (!audioBase64 && Array.isArray(json.steps)) {
    for (let i = json.steps.length - 1; i >= 0; i--) {
      const step = json.steps[i];
      if (step?.type === 'model_output' && Array.isArray(step.content)) {
        const audioBlock = [...step.content].reverse().find((c: { type?: string; data?: string }) => c?.type === 'audio' && c?.data);
        if (audioBlock) {
          audioBase64 = audioBlock.data;
          break;
        }
      }
    }
  }

  if (!audioBase64) {
    return { status: 502, body: { error: 'No audio returned by Gemini TTS.' } };
  }

  return {
    status: 200,
    body: { audioBase64, mimeType: 'audio/wav', voice },
  };
}
