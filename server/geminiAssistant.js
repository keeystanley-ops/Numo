const fs = require('fs');
const path = require('path');

const GEMINI_MODEL = 'gemini-2.5-flash';
const GEMINI_ENDPOINT = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;

// Per-user isolated conversation history & pending action store
const userConversations = new Map(); // userId -> Array<{ role: 'user'|'model', parts: Array }>
const userPendingActions = new Map(); // userId -> Map<actionId, actionPayload>

/**
 * Load .env from project root into process.env if not already set.
 * Never logs or exposes secret values.
 */
function loadDotEnvIfPresent() {
  try {
    const envPath = path.join(__dirname, '..', '.env');
    if (!fs.existsSync(envPath)) return;
    const lines = fs.readFileSync(envPath, 'utf8').split(/\r?\n/);
    for (const rawLine of lines) {
      const line = rawLine.trim();
      if (!line || line.startsWith('#')) continue;
      const eqIdx = line.indexOf('=');
      if (eqIdx <= 0) continue;
      const key = line.slice(0, eqIdx).trim();
      let val = line.slice(eqIdx + 1).trim();
      if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
        val = val.slice(1, -1);
      }
      if (!process.env[key]) {
        process.env[key] = val;
      }
    }
  } catch (_) {
    // Ignore .env read errors; rely on process.env
  }
}

function getGeminiApiKey() {
  loadDotEnvIfPresent();
  const key = (process.env.GEMINI_API_KEY || '').trim();
  if (!key || key === 'MY_GEMINI_API_KEY') return null;
  return key;
}

const GEMINI_TOOLS = [
  {
    functionDeclarations: [
      // Read-only financial intelligence tools
      {
        name: 'getAccountBalances',
        description: 'Get the authenticated user\'s linked bank, mobile money (M-PESA), MMF, SACCO, and cash account balances.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getAvailableCash',
        description: 'Get the user\'s total liquid available cash balance and currency.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getTransactions',
        description: 'Get recent transactions, optionally filtered by category or limit.',
        parameters: {
          type: 'OBJECT',
          properties: {
            category: { type: 'STRING', description: 'Optional category filter, e.g. Food & Dining, Transport' },
            limit: { type: 'INTEGER', description: 'Maximum number of recent transactions to return (default 5)' }
          }
        }
      },
      {
        name: 'getBudgetStatus',
        description: 'Get monthly budget status overall or for a specific spending category.',
        parameters: {
          type: 'OBJECT',
          properties: {
            category: { type: 'STRING', description: 'Optional category name such as Food, Transport, Shopping, Bills' }
          }
        }
      },
      {
        name: 'getUpcomingBills',
        description: 'Get upcoming bills, amounts, due dates, and total upcoming bill obligations.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getSavingsGoals',
        description: 'Get all savings goals, saved amounts, target amounts, and remaining amounts.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getDebtSummary',
        description: 'Get active debts, total debt owed, minimum monthly payments, and interest rates.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getInvestmentPortfolio',
        description: 'Get investment holdings, total portfolio valuation, invested capital, and total return.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getDividendForecast',
        description: 'Get projected annual and monthly dividend/yield income from investments.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getNetWorth',
        description: 'Get the user\'s total net worth combining cash, investments, and liabilities.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      {
        name: 'getSpendingAnalytics',
        description: 'Get category-by-category spending breakdown and overspending insights.',
        parameters: { type: 'OBJECT', properties: {} }
      },
      // Controlled action tools (never mutate directly; prepare confirmation card)
      {
        name: 'createTransaction',
        description: 'Propose recording a new expense or income transaction. Requires explicit user confirmation.',
        parameters: {
          type: 'OBJECT',
          properties: {
            amount: { type: 'NUMBER', description: 'Transaction amount' },
            description: { type: 'STRING', description: 'Short description or merchant/item name, e.g. Lunch' },
            account: { type: 'STRING', description: 'Account name such as M-PESA, Bank Account, Cash' },
            category: { type: 'STRING', description: 'Category such as Food & Dining, Transport, Shopping, Bills & Utilities, Income' },
            isIncome: { type: 'BOOLEAN', description: 'True if income/deposit, false if expense' }
          },
          required: ['amount', 'description', 'account', 'category', 'isIncome']
        }
      },
      {
        name: 'updateTransaction',
        description: 'Propose updating an existing transaction category or amount. Requires explicit user confirmation.',
        parameters: {
          type: 'OBJECT',
          properties: {
            title: { type: 'STRING', description: 'Existing transaction title to update' },
            newCategory: { type: 'STRING', description: 'Updated category' },
            newAmount: { type: 'NUMBER', description: 'Optional updated amount' }
          },
          required: ['title']
        }
      },
      {
        name: 'createBudget',
        description: 'Propose setting or updating a monthly budget limit for a category. Requires explicit user confirmation.',
        parameters: {
          type: 'OBJECT',
          properties: {
            category: { type: 'STRING', description: 'Budget category name' },
            monthlyLimit: { type: 'NUMBER', description: 'Proposed monthly budget limit' }
          },
          required: ['category', 'monthlyLimit']
        }
      },
      {
        name: 'updateSavingsGoal',
        description: 'Propose contributing funds toward a savings goal. Requires explicit user confirmation.',
        parameters: {
          type: 'OBJECT',
          properties: {
            goalTitle: { type: 'STRING', description: 'Savings goal name, e.g. Emergency Fund' },
            amount: { type: 'NUMBER', description: 'Contribution amount' },
            account: { type: 'STRING', description: 'Source account, e.g. M-PESA' }
          },
          required: ['goalTitle', 'amount']
        }
      },
      {
        name: 'calculateDebtStrategy',
        description: 'Calculate and explain an avalanche vs snowball debt payoff recommendation using current debts.',
        parameters: {
          type: 'OBJECT',
          properties: {
            extraMonthlyPayment: { type: 'NUMBER', description: 'Optional extra monthly payment toward debt' }
          }
        }
      },
      {
        name: 'updateAssetValuation',
        description: 'Propose updating an investment or asset valuation. Requires explicit user confirmation.',
        parameters: {
          type: 'OBJECT',
          properties: {
            assetName: { type: 'STRING', description: 'Asset or holding name' },
            newValuation: { type: 'NUMBER', description: 'Updated valuation amount' }
          },
          required: ['assetName', 'newValuation']
        }
      }
    ]
  }
];

const ACTION_TOOLS = new Set([
  'createTransaction',
  'updateTransaction',
  'createBudget',
  'updateSavingsGoal',
  'updateAssetValuation'
]);

function executeReadTool(name, args, snapshot) {
  const currency = snapshot.currency || 'KES';
  const categories = Array.isArray(snapshot.categories) ? snapshot.categories : [];
  const transactions = Array.isArray(snapshot.transactions) ? snapshot.transactions : [];
  const bills = Array.isArray(snapshot.bills) ? snapshot.bills : [];
  const goals = Array.isArray(snapshot.goals) ? snapshot.goals : [];
  const debts = Array.isArray(snapshot.debts) ? snapshot.debts : [];
  const holdings = Array.isArray(snapshot.investmentHoldings) ? snapshot.investmentHoldings : [];
  const accounts = Array.isArray(snapshot.accounts) ? snapshot.accounts : [];

  switch (name) {
    case 'getAccountBalances':
      return { currency, accounts };
    case 'getAvailableCash':
      return { currency, availableCash: snapshot.availableCash ?? 0 };
    case 'getTransactions': {
      const cat = (args?.category || '').toLowerCase();
      const limit = Math.max(1, Math.min(20, Number(args?.limit) || 5));
      const filtered = cat
        ? transactions.filter(t => String(t.category || '').toLowerCase().includes(cat) || String(t.title || '').toLowerCase().includes(cat))
        : transactions;
      return { currency, transactions: filtered.slice(0, limit) };
    }
    case 'getBudgetStatus': {
      const cat = (args?.category || '').toLowerCase();
      if (cat) {
        const matched = categories.filter(c => String(c.name || '').toLowerCase().includes(cat));
        return {
          currency,
          monthlyBudgetTarget: snapshot.monthlyBudgetTarget ?? 0,
          matchedCategories: matched
        };
      }
      return {
        currency,
        monthlyBudgetTarget: snapshot.monthlyBudgetTarget ?? 0,
        totalSpent: snapshot.totalSpent ?? 0,
        totalRemaining: snapshot.totalRemaining ?? 0,
        categories
      };
    }
    case 'getUpcomingBills': {
      const totalUpcoming = bills.reduce((sum, b) => sum + Number(b.amount || 0), 0);
      return { currency, totalUpcoming, bills };
    }
    case 'getSavingsGoals':
      return { currency, goals };
    case 'getDebtSummary': {
      const totalDebt = debts.reduce((sum, d) => sum + Number(d.balance || 0), 0);
      return { currency, totalDebt, debts };
    }
    case 'getInvestmentPortfolio':
      return {
        currency,
        totalPortfolioValuation: snapshot.totalPortfolioValuation ?? 0,
        totalInvestedCapital: snapshot.totalInvestedCapital ?? 0,
        totalPortfolioReturn: snapshot.totalPortfolioReturn ?? 0,
        totalPortfolioReturnPercent: snapshot.totalPortfolioReturnPercent ?? 0,
        holdings
      };
    case 'getDividendForecast': {
      const totalVal = Number(snapshot.totalPortfolioValuation || 0);
      const estimatedAnnualDividends = Math.round(totalVal * 0.085);
      return {
        currency,
        estimatedAnnualDividends,
        estimatedMonthlyDividends: Math.round(estimatedAnnualDividends / 12),
        note: 'Estimated from current portfolio holdings yield'
      };
    }
    case 'getNetWorth': {
      const totalDebt = debts.reduce((sum, d) => sum + Number(d.balance || 0), 0);
      return {
        currency,
        totalNetWorth: snapshot.totalNetWorth ?? 0,
        availableCash: snapshot.availableCash ?? 0,
        totalPortfolioValuation: snapshot.totalPortfolioValuation ?? 0,
        totalDebt
      };
    }
    case 'getSpendingAnalytics': {
      const sorted = [...categories].sort((a, b) => Number(b.spent || 0) - Number(a.spent || 0));
      return {
        currency,
        totalSpent: snapshot.totalSpent ?? 0,
        monthlyBudgetTarget: snapshot.monthlyBudgetTarget ?? 0,
        topCategories: sorted
      };
    }
    case 'calculateDebtStrategy': {
      const extra = Number(args?.extraMonthlyPayment || 0);
      const sortedAvalanche = [...debts].sort((a, b) => Number(b.apr || 0) - Number(a.apr || 0));
      const sortedSnowball = [...debts].sort((a, b) => Number(a.balance || 0) - Number(b.balance || 0));
      return {
        currency,
        extraMonthlyPayment: extra,
        avalancheOrder: sortedAvalanche,
        snowballOrder: sortedSnowball
      };
    }
    default:
      return { error: `Unknown tool: ${name}` };
  }
}

function buildPendingAction(name, args, snapshot) {
  const id = `act_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
  const currency = snapshot.currency || 'KES';
  switch (name) {
    case 'createTransaction':
      return {
        id,
        kind: 'TRANSACTION',
        amount: Number(args.amount || 0),
        description: String(args.description || 'Transaction'),
        account: String(args.account || 'M-PESA'),
        category: String(args.category || 'Other'),
        isIncome: Boolean(args.isIncome),
        currency,
        statusLabel: 'Awaiting Approval'
      };
    case 'updateSavingsGoal':
      return {
        id,
        kind: 'CONTRIBUTE_GOAL',
        amount: Number(args.amount || 0),
        description: `Contribute to ${args.goalTitle || 'Savings Goal'}`,
        account: String(args.account || 'M-PESA'),
        category: 'Savings Goal',
        isIncome: false,
        currency,
        statusLabel: 'Awaiting Approval'
      };
    case 'createBudget':
      return {
        id,
        kind: 'CREATE_BUDGET',
        amount: Number(args.monthlyLimit || 0),
        description: `Set ${args.category || 'Category'} budget`,
        account: 'Monthly Budget',
        category: String(args.category || 'Other'),
        isIncome: false,
        currency,
        statusLabel: 'Awaiting Approval'
      };
    case 'updateTransaction':
      return {
        id,
        kind: 'UPDATE_TRANSACTION',
        amount: Number(args.newAmount || 0),
        description: `Update "${args.title || 'Transaction'}"`,
        account: 'Ledger',
        category: String(args.newCategory || 'Other'),
        isIncome: false,
        currency,
        statusLabel: 'Awaiting Approval'
      };
    case 'updateAssetValuation':
      return {
        id,
        kind: 'UPDATE_ASSET',
        amount: Number(args.newValuation || 0),
        description: `Update ${args.assetName || 'Asset'} valuation`,
        account: 'Investments',
        category: 'Portfolio',
        isIncome: true,
        currency,
        statusLabel: 'Awaiting Approval'
      };
    default:
      return null;
  }
}

async function callGeminiApi(apiKey, payload) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try {
    const response = await fetch(GEMINI_ENDPOINT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-goog-api-key': apiKey
      },
      body: JSON.stringify(payload),
      signal: controller.signal
    });
    const text = await response.text();
    let json = null;
    try {
      json = JSON.parse(text);
    } catch (_) {
      json = null;
    }
    return { ok: response.ok, status: response.status, json };
  } finally {
    clearTimeout(timeout);
  }
}

function mapGeminiHttpError(status, json) {
  const msg = json?.error?.message || '';
  if (status === 400 || status === 401 || status === 403) {
    if (msg.toLowerCase().includes('api key')) {
      return 'Hey Numo could not authenticate with Gemini. Please verify the configured GEMINI_API_KEY on the server.';
    }
    return 'Hey Numo could not process that request. Please try rephrasing your question.';
  }
  if (status === 429) {
    return 'Hey Numo is receiving too many requests right now. Please wait a few seconds and try again.';
  }
  return 'Hey Numo is temporarily unable to reach Gemini AI. Please check your connection and try again.';
}

async function handleHeyNumoChat({ userId, message, financialSnapshot, resetHistory }) {
  const cleanUserId = String(userId || '').trim();
  if (!cleanUserId) {
    return { status: 401, body: { error: 'Authenticated userId is required.' } };
  }
  const cleanMessage = String(message || '').trim();
  if (!cleanMessage) {
    return { status: 400, body: { error: 'Message cannot be empty.' } };
  }

  const apiKey = getGeminiApiKey();
  if (!apiKey) {
    return {
      status: 503,
      body: {
        errorCode: 'GEMINI_KEY_MISSING',
        reply: 'Hey Numo AI is waiting for GEMINI_API_KEY to be configured in the server environment.'
      }
    };
  }

  if (resetHistory) {
    userConversations.delete(cleanUserId);
  }

  const history = userConversations.get(cleanUserId) || [];
  const snapshot = financialSnapshot || {};
  const currency = snapshot.currency || 'KES';

  const systemInstruction = {
    parts: [
      {
        text: [
          'You are Hey Numo, the intelligent personal finance voice and chat assistant inside the Numo Personal Finance OS.',
          `The authenticated user prefers currency ${currency}.`,
          'Always use the provided financial tools to inspect real account balances, transactions, budgets, bills, savings goals, debts, investments, and net worth. Never invent numbers.',
          'For state-modifying actions (recording/updating transactions, budgets, savings contributions, or asset valuations), call the corresponding action tool so the app can show an Awaiting Approval confirmation card. Never claim a transaction was already executed before the user confirms.',
          'Keep spoken/chat replies concise, warm, accurate, and easy to read aloud (2-4 sentences unless a breakdown is requested).'
        ].join(' ')
      }
    ]
  };

  const workingContents = [
    ...history.slice(-16),
    { role: 'user', parts: [{ text: cleanMessage }] }
  ];

  let pendingAction = null;
  let finalReplyText = '';

  try {
    for (let round = 0; round < 4; round++) {
      const result = await callGeminiApi(apiKey, {
        systemInstruction,
        contents: workingContents,
        tools: GEMINI_TOOLS
      });

      if (!result.ok) {
        return {
          status: result.status >= 400 && result.status < 600 ? result.status : 502,
          body: {
            errorCode: 'GEMINI_API_ERROR',
            reply: mapGeminiHttpError(result.status, result.json)
          }
        };
      }

      const candidate = result.json?.candidates?.[0];
      const modelContent = candidate?.content;
      const parts = Array.isArray(modelContent?.parts) ? modelContent.parts : [];

      if (parts.length > 0) {
        workingContents.push({ role: 'model', parts });
      }

      const functionCalls = parts.filter(p => p.functionCall);
      if (functionCalls.length === 0) {
        finalReplyText = parts
          .map(p => p.text || '')
          .join(' ')
          .trim();
        break;
      }

      const responseParts = [];
      for (const part of functionCalls) {
        const fc = part.functionCall;
        const toolName = fc.name;
        const toolArgs = fc.args || {};

        if (ACTION_TOOLS.has(toolName)) {
          pendingAction = buildPendingAction(toolName, toolArgs, snapshot);
          if (pendingAction) {
            const userMap = userPendingActions.get(cleanUserId) || new Map();
            userMap.set(pendingAction.id, pendingAction);
            userPendingActions.set(cleanUserId, userMap);
          }
          responseParts.push({
            functionResponse: {
              name: toolName,
              response: {
                status: 'AWAITING_USER_CONFIRMATION',
                requiresConfirmation: true,
                preparedAction: pendingAction
              }
            }
          });
        } else {
          const toolResult = executeReadTool(toolName, toolArgs, snapshot);
          responseParts.push({
            functionResponse: {
              name: toolName,
              response: toolResult
            }
          });
        }
      }

      workingContents.push({ role: 'user', parts: responseParts });
    }

    if (!finalReplyText) {
      finalReplyText = pendingAction
        ? `I've prepared this ${pendingAction.description} for ${pendingAction.currency} ${pendingAction.amount}. Please tap Confirm to execute it.`
        : 'I checked your Numo accounts, but could not generate a clear response. Please try asking again.';
    }

    // Save clean user/model text turns to isolated user conversation history
    const updatedHistory = [
      ...history,
      { role: 'user', parts: [{ text: cleanMessage }] },
      { role: 'model', parts: [{ text: finalReplyText }] }
    ].slice(-20);
    userConversations.set(cleanUserId, updatedHistory);

    return {
      status: 200,
      body: {
        reply: finalReplyText,
        spokenText: finalReplyText,
        pendingAction
      }
    };
  } catch (err) {
    const isAbort = err && err.name === 'AbortError';
    return {
      status: 504,
      body: {
        errorCode: isAbort ? 'GEMINI_TIMEOUT' : 'NETWORK_ERROR',
        reply: isAbort
          ? 'Hey Numo took a bit too long to respond. Please try again.'
          : 'Network interruption while reaching Hey Numo AI. Please try again.'
      }
    };
  }
}

function handleConfirmAction({ userId, actionId, confirmed }) {
  const cleanUserId = String(userId || '').trim();
  if (!cleanUserId || !actionId) {
    return { status: 400, body: { error: 'userId and actionId are required.' } };
  }
  const userMap = userPendingActions.get(cleanUserId);
  if (!userMap || !userMap.has(actionId)) {
    return { status: 404, body: { error: 'Pending action not found for this user.' } };
  }
  const action = userMap.get(actionId);
  userMap.delete(actionId);
  return {
    status: 200,
    body: {
      actionId,
      status: confirmed ? 'CONFIRMED' : 'CANCELLED',
      action
    }
  };
}

module.exports = {
  handleHeyNumoChat,
  handleConfirmAction,
  hasConfiguredGeminiKey: () => Boolean(getGeminiApiKey())
};
