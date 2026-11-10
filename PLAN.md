# Numo — Personal Finance & Wealth Operating System
### Comprehensive Architecture & Implementation Specification (Universal Expo Router)

Numo is a privacy-first personal finance and wealth management application uniting two distinct environments: **Money Management** (daily income, expenses, budgets, savings goals, debt, and cash flow) and **Investment Management** (multi-asset portfolios, holdings, dividends, interest, performance, and asset allocation).

Both environments share a unified financial engine, a double-entry ledger, and a hands-free conversational AI assistant (**"Hey Numo"**). The application is scaffolded inside **`apps/mobile`** using **Expo Managed Workflow** with **Expo Router universal multi-platform support** (powering iOS, Android, and Web from a single unified codebase). It is backed by a serverless **tRPC backend (Next.js / Hono)**, a **Neon PostgreSQL** database managed by **Drizzle ORM**, **Clerk** authentication with on-device biometrics, **ImageKit.io** media optimization, **Sentry** full-stack telemetry, and **CodeRabbit** automated code review.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions are established and locked in:
> - **Product Identity**: **Numo** — Kenya-first, globally expandable personal finance operating system.
> - **Workspace Layout**: Application scaffolded inside **`apps/mobile`**, keeping the workspace organized and future-proof.
> - **Universal Cross-Platform**: **Expo Router built-in web support** (`"web": { "bundler": "metro" }`), allowing the exact same codebase and file-based routes to deploy natively to iOS, Android, and Web browsers.
> - **Backend API**: Serverless **Next.js or Hono API** utilizing end-to-end type-safe **tRPC**.
> - **Database & ORM**: **Neon PostgreSQL** with pooled (`DATABASE_URL`) & unpooled (`DATABASE_URL_UNPOOLED`) configurations and **Drizzle ORM**, enforcing a strict **double-entry immutable ledger**. Linked to Neon project `old-bar-99839974` (`production` branch) with installed Neon coding agent skills (`.agents/skills/neon*`).
> - **Authentication & Device Security**: Mandatory **Clerk** cloud sign-in on first launch, paired with **Expo Local Authentication** (Biometrics / PIN) for quick, secure subsequent unlocks.
> - **Media Pipeline**: Client-direct receipt and document uploads to **ImageKit.io** using backend-signed HMAC authentication tokens.
> - **Voice Assistant ("Hey Numo")**: Hands-free in-app wake-word activation (no physical button), backed by an AI API with **multi-turn conversational memory**, proactive follow-up questions, and mandatory **action confirmation preview modals**.
> - **Observability & CI/CD**: Full-stack **Sentry** (crash reporting, distributed tracing, session replay) and **GitHub Actions** with **CodeRabbit** PR reviews and automated Drizzle migration verification.
> - **Network Resilience**: Read-only local cache with an informative offline banner when internet access drops.

---

## 1. Directory Structure (`apps/mobile`)

```
apps/mobile/
├── app.json                       # Universal Expo configuration (iOS, Android, Web)
├── package.json                   # Dependencies: Expo, Expo Router, Clerk, Sentry, Drizzle
├── tsconfig.json                  # TypeScript path aliases
├── babel.config.js
├── app/                           # Universal Expo Router file-based routes
│   ├── _layout.tsx                # Root provider tree (ClerkProvider, Sentry, Theme, Gestures)
│   ├── index.tsx                  # Environment entry router (restores last-used state)
│   ├── (auth)/                    # Clerk authentication routes
│   │   ├── _layout.tsx
│   │   ├── sign-in.tsx            # Email magic link, SMS OTP, Google OAuth
│   │   ├── sign-up.tsx
│   │   └── lock-screen.tsx        # Face ID / Touch ID / Device PIN unlock
│   ├── (onboarding)/              # 8-Step First-Use Onboarding Wizard
│   │   ├── _layout.tsx
│   │   ├── 1-welcome.tsx          # Dual-environment concept & pull-down tutorial
│   │   ├── 2-security.tsx         # Biometric & local PIN configuration
│   │   ├── 3-localization.tsx     # Country (Kenya), Currency (KES), Date formatting
│   │   ├── 4-accounts.tsx         # Cash, M-PESA, Bank, Savings starting balances
│   │   ├── 5-budget.tsx           # Category, Flexible, or Zero-based starting budget
│   │   ├── 6-goals-debt.tsx       # Emergency runway multiplier & loan tracking
│   │   ├── 7-investments.tsx      # Starting investment accounts & holdings
│   │   └── 8-launch.tsx           # Transition to preferred starting environment
│   ├── (tabs)/                    # Dual-environment shell
│   │   ├── _layout.tsx            # Top Pull-down zone, Environment Pill Switcher, Voice trigger
│   │   ├── money/                 # Money Management Environment
│   │   │   ├── index.tsx          # Overview: Available cash, cash flow gauge, bills
│   │   │   ├── transactions.tsx   # Ledger, search, multi-category splits, personal/biz tags
│   │   │   ├── budgets.tsx        # Budgets, pay-cycle, active overspending rebalancer
│   │   │   ├── goals-debt.tsx     # Savings runway, AI debt payoff advisor (Avalanche vs Snowball)
│   │   │   └── insights.tsx       # Donut charts, merchant rankings, needs vs wants
│   │   └── investments/           # Investment Management Environment
│   │       ├── index.tsx          # Portfolio dashboard, invested capital, freshness badges
│   │       ├── holdings.tsx       # Multi-asset cards (NSE, MMF, T-Bills, SACCOs, Bonds, Crypto)
│   │       ├── holding/[id].tsx   # Asset detail isolating USD capital gain vs KES forex impact
│   │       ├── activity.tsx       # Buy, sell, dividend, fee, withholding tax activity
│   │       ├── income.tsx         # Projected dividend & coupon calendar from yields
│   │       └── analysis.tsx       # Allocation donut, concentration risk analytics
│   ├── modal/
│   │   ├── add-transaction.tsx    # Fast entry, calculator, ImageKit receipt upload
│   │   ├── add-holding.tsx        # New investment holding wizard
│   │   ├── update-valuation.tsx   # Append-only manual valuation adjustment modal
│   │   ├── what-if-simulator.tsx  # Multi-variable financial scenario simulator
│   │   └── net-worth-sheet.tsx    # Total vs Investable Net Worth breakdown
│   └── assistant/
│       └── hey-numo-sheet.tsx     # Hands-free voice overlay, multi-turn memory, confirmation card
├── components/                    # Reusable universal UI components
│   ├── ui/                        # Buttons, glass cards, segmented pills, keypad
│   ├── charts/                    # Donut charts, cash flow bars, net worth timelines
│   └── ledger/                    # Tabular financial entry rows, transaction items
├── lib/                           # Core utilities & API clients
│   ├── trpc.ts                    # tRPC client with Clerk auth headers
│   ├── imagekit.ts                # Direct client upload with backend HMAC signatures
│   ├── double-entry.ts            # Strict accounting engine (debit/credit validation)
│   └── voice.ts                   # Keyword spotter ("Hey Numo") & speech recognition
└── db/                            # Drizzle ORM schema & migrations
    ├── schema.ts                  # Neon PostgreSQL table definitions
    └── index.ts                   # Pooled database client connection
```

---

## 2. System Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                               APPS/MOBILE (EXPO ROUTER UNIVERSAL)                           │
│                             Runs on iOS, Android, and Web Browsers                          │
├──────────────────────────────────────────────┬──────────────────────────────────────────────┤
│            MONEY MANAGEMENT ENV              │          INVESTMENT MANAGEMENT ENV           │
│  - Available Cash vs Reserved Obligations    │  - Consolidated & Multi-Portfolio Holdings   │
│  - Budgets (Category, Flex, Zero-based)      │  - Projected Dividend & Coupon Calendar      │
│  - Active Overspending Rebalancing Prompts   │  - Asset Allocation & Concentration Risk     │
│  - Goals Runway (3, 6, 9 Month Multipliers)  │  - Isolated Asset Capital vs Forex Returns   │
│  - AI-Driven Debt Payoff Advisor             │  - Append-Only Valuation Snapshot History    │
├──────────────────────────────────────────────┴──────────────────────────────────────────────┤
│                      SIGNATURE INTERACTION: PULL-DOWN & PILL SWITCHER                       │
│    - 72dp drag threshold in top zone with haptic feedback & morphing transition             │
│    - State-preserving screen stack restoration                                              │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│                          CONVERSATIONAL AI ASSISTANT ("Hey Numo")                           │
│    - Hands-free in-app keyword detector ("Hey Numo" - no physical button)                  │
│    - Multi-turn conversational session memory with proactive follow-up questions            │
│    - Mandatory Action Confirmation Preview Modal before database writes                     │
├──────────────────────────────────────────────┬──────────────────────────────────────────────┤
│          AUTHENTICATION & SECURITY           │             OFFLINE RESILIENCE               │
│  - Clerk SDK (Cloud Sign-In / OAuth)         │  - TanStack Query (React Query) Cache        │
│  - Expo Local Authentication (FaceID/PIN)    │  - Read-Only Mode with Offline Status Banner │
└───────────────────────────────────────▲──────┴──────────────────────────────────────────────┘
                                        │ HTTPS / tRPC + Signed Tokens
┌───────────────────────────────────────▼─────────────────────────────────────────────────────┐
│                               SERVERLESS BACKEND API                                        │
│                           (Next.js / Hono on Edge / Node)                                   │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│  - tRPC Router (Routers: auth, accounts, transactions, budgets, investments, assistant)     │
│  - Clerk Webhook & JWT Session Validator                                                    │
│  - ImageKit.io HMAC Upload Token Generator                                                  │
│  - Sentry Distributed Tracing & Exception Logging                                           │
│  - AI LLM Completion Gateway (Intent Extraction, Multi-Turn Memory, Debt Strategy)         │
└───────────────────────────────────────▲─────────────────────────────────────────────────────┘
                                        │ Pooled WebSocket / TCP
┌───────────────────────────────────────▼─────────────────────────────────────────────────────┐
│                           NEON POSTGRESQL + DRIZZLE ORM                                     │
│                         (Serverless Pooled Database Layer)                                  │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│  - users                    - accounts                - ledger_entries (Double-Entry)       │
│  - transactions             - transaction_splits      - budgets & allocations               │
│  - savings_goals            - debts                   - holdings                            │
│  - valuation_snapshots      - investment_activities   - assistant_sessions & memory         │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. The 8-Step First-Use Onboarding Sequence

1. **Step 1: Welcome & Clerk Authentication**:
   - Welcome hero introducing Numo's dual-environment operating system.
   - Clerk authentication via Email magic link, SMS OTP, or Google/Apple sign-in.
   - User entity synchronized into Neon Postgres.
2. **Step 2: Biometric & Local PIN Security Setup**:
   - Enable Face ID, Touch ID, or device PIN via `expo-local-authentication`.
   - Explains: *"Secure your financial records so only you can unlock the app on this device."*
3. **Step 3: Financial Localization & Settings**:
   - Country selector (default: **Kenya**).
   - Primary currency (default: **KES**, supporting USD, EUR, GBP, TZS, UGX).
   - Regional formatting (`DD/MM/YYYY`) and hands-free "Hey Numo" microphone permission request.
4. **Step 4: Setup Money Accounts**:
   - Quick-add common Kenyan account types: **Cash on Hand**, **M-PESA Wallet**, **Bank Accounts** (Equity, KCB, Standard Chartered, Absa), and **Savings**.
   - Input starting balances (with explicit note: *Starting balances represent existing assets and do NOT inflate monthly income*).
5. **Step 5: Initial Budget Creation**:
   - Choice of method: Category Limits, Flexible Budget, or Zero-Based Budgeting.
   - Pre-populated Kenyan spending categories: Rent, Food, Matatu/Fuel, KPLC/Water, Airtime/Data, Entertainment, Healthcare (SHA).
6. **Step 6: Savings Goals & Debt (Optional)**:
   - Configure an **Emergency Fund Runway** (auto-calculates 3, 6, or 9 months of essential expenses).
   - Option to log existing liabilities (e.g. HELB student loans, bank loans, mobile loans).
7. **Step 7: Setup Investment Portfolio (Optional)**:
   - Create initial investment accounts (NSE Brokerage, Money Market Fund e.g. Sanlam/Britam/CIC, SACCO e.g. Stima/Harambee, Treasury Bills/Bonds).
   - Log first holdings or record starting invested capital.
8. **Step 8: Enter Preferred Environment**:
   - Choose initial landing environment (Money Management or Investment Management).
   - Smooth animated transition directly into the live dashboard.

---

## 4. Screen-by-Screen Layout & Component Breakdown

### 4.1 Signature Interaction: Pull-Down Environment Switcher
- **Dedicated Interaction Zone**: 48dp zone anchored beneath the system status bar.
- **Gesture Dynamics**: Vertical drag exceeding 72dp triggers haptic feedback (`Haptics.impactAsync(ImpactFeedbackStyle.Medium)`) and activates a smooth shared-element morphing transition swapping the active environment.
- **Scroll Conflict Isolation**: Vertical scrolling in nested lists (transactions, holdings) is strictly prevented from triggering the switcher.
- **Accessibility Fallback**: Top app bar Environment Pill Switcher (`[Money | Investments]`) provides instant one-tap switching.
- **State Preservation**: Switching preserves each environment's internal navigation stack, scroll offset, and filter states.

### 4.2 Money Management Environment
- **Screen 1: Overview (Dashboard)**: Available Cash card, monthly cash flow gauge (Income vs. Spent vs. Saved), budget health meters, upcoming obligations reel, and recent activity feed.
- **Screen 2: Transactions**: Search and multi-filter bar, date-grouped list with category avatars and M-PESA chips, and transaction sheet with **Multi-Category Splitting** and **Personal vs. Business Tagging** per line.
- **Screen 3: Budgets**: Category limits, flexible budgets, and zero-based budgeting with pay-cycle support (e.g. 25th payday cycle) and an **Active Overspending Rebalancer** prompting fund shifts when categories exceed limits.
- **Screen 4: Goals & Debt**: Savings goal cards with circular progress, dynamic emergency fund runway (3, 6, 9 months of essential spend), and an **AI Debt Payoff Advisor** comparing Avalanche and Snowball repayment roadmaps.
- **Screen 5: Insights**: Interactive SVG donut chart with transaction drill-down, top merchant leaderboard, 50/30/20 needs vs. wants ratio, and monthly cash flow history.

### 4.3 Investment Management Environment
- **Screen 1: Portfolio Dashboard**: Total portfolio valuation, invested capital, total return %, valuation freshness badges (*"Valued 2 days ago"*), and upcoming 30-day dividend strip.
- **Screen 2: Holdings**: Directory for Stocks (NSE & Global), MMF, Treasury Bills, Bonds, SACCO, REITs, Crypto, Real Estate. Foreign assets cleanly isolate **USD asset price return** from **KES foreign exchange currency movement**.
- **Screen 3: Activity Ledger**: Filterable transaction log of buys, sells, deposits, withdrawals, dividend distributions, fees, and withholding tax deductions.
- **Screen 4: Income & Dividends**: Monthly, quarterly, and annual investment income run-rate, paired with an income calendar plotting confirmed receipts and future estimated dates projected from entered dividend yields.
- **Screen 5: Analysis & Risk**: Asset class allocation donut and portfolio concentration alerts (*"Top 3 holdings represent 64% of portfolio"*).

### 4.4 Conversational Voice Assistant ("Hey Numo")
- **Hands-Free Activation**: In-app continuous keyword detection listening for **"Hey Numo"** while the app is in the foreground. No physical microphone button required.
- **Assistant Sheet (Overlay Modal)**:
  - Ambient listening animation and real-time voice transcription.
  - **Multi-Turn Session Memory**: Retains conversation context across exchanges.
  - **Proactive Follow-Ups**: Prompts for missing fields when user instructions are ambiguous.
  - **Action Confirmation Card**: Displays an explicit preview card before any database write (Amount, Category, Account, Date). Never commits without tapping **Confirm**.

---

## 5. Strict Double-Entry Ledger & Financial Accounting Rules

1. **Rule 1: Transfers are NEVER Income or Expenses**: Moving funds between accounts (e.g. Bank to M-PESA) creates paired debit/credit entries leaving liquid cash and income/expense totals unchanged.
2. **Rule 2: Investment Contributions are NOT Ordinary Expenses**: Moving cash to an investment account debits the asset holding and credits cash, increasing invested capital without distorting living expenses.
3. **Rule 3: Debt Payments Separate Principal and Interest**: Repayments strictly separate liability balance reduction (principal debit) from living expense (interest fee debit).
4. **Rule 4: Dividends & Interest Classified as Investment Income**: Received income credits Investment Income and debits cash without inflating asset cost basis or distorting net worth.
5. **Rule 5: Asset Valuation Changes are NOT Cash Income**: Property/stock adjustments record unrealized gains/losses; never counted as liquid cash.
6. **Rule 6: Sale Proceeds Distinguished from Capital Gains**: Asset liquidations distinguish gross proceeds, acquisition cost basis, fees, and net realized gain/loss.
7. **Rule 7: Currency Integrity**: Foreign holdings store original currency values and exchange rates, displaying KES equivalent without overwriting original records.
8. **Rule 8: Immutable Audit Trail**: All adjustments append audit records with timestamps rather than silently mutating past reports.

---

## 6. The 10 Practical Acceptance Test Scenarios

- **Scenario 1 (First-Time User)**: Signs up via Clerk, sets PIN, selects Kenya & KES, inputs KES 50,000 cash balance, creates food budget, and opens Investments. Verified: Starting balance is credited to Cash without generating artificial monthly income.
- **Scenario 2 (Record an Expense)**: Records KES 500 Food expense from M-PESA. Verified: M-PESA decreases by 500, Food budget spent increases by 500, and ledger records Debit: Food, Credit: M-PESA.
- **Scenario 3 (Transfer Money)**: Transfers KES 2,000 from Bank to M-PESA. Verified: Bank decreases by 2,000, M-PESA increases by 2,000, and cash assets and living expenses remain strictly unchanged.
- **Scenario 4 (Invest Money)**: Moves KES 5,000 from Bank into Investment Account for NSE shares. Verified: Cash decreases by 5,000, investment holding increases by 5,000; net worth preserved.
- **Scenario 5 (Receive a Dividend)**: Records KES 1,000 dividend deposited into Cash. Verified: Cash increases by 1,000, Investment Income records 1,000, Net Worth increases by 1,000 with zero double-counting.
- **Scenario 6 (Pay Debt)**: Pays KES 3,000 on loan (KES 2,500 principal + KES 500 interest). Verified: Debt decreases by 2,500, interest expense logs 500, cash decreases by 3,000.
- **Scenario 7 (Use Voice Assistant)**: Speaks "Hey Numo, record 600 shillings for transport". Verified: Assistant activates hands-free, presents Action Confirmation Modal, and commits only upon tapping Confirm.
- **Scenario 8 (Switch Environments)**: Drags down 72dp from the top interaction zone in Money Management. Verified: UI morphs into Investment Management with haptic feedback; scrolling lists never triggers switching.
- **Scenario 9 (Offline Operation)**: Toggles Airplane Mode. Verified: App transitions to read-only mode, displays offline status banner, and presents previously cached balances without crashes.
- **Scenario 10 (Recover Financial Data)**: Logs in on a secondary device via Clerk. Verified: Neon Postgres streams complete historical records; all data is verified identical with zero duplicates.

---

## 7. Phased Implementation Roadmap

1. **Phase 1: Project Scaffolding in `apps/mobile`**:
   - Initialize Expo project with Expo Router, TypeScript, and universal web support (`"web": { "bundler": "metro" }`).
   - Configure `@clerk/clerk-expo`, `expo-local-authentication`, `@sentry/react-native`, and `imagekit-javascript`.
2. **Phase 2: Database Schema & Double-Entry Ledger (`db/schema.ts`)**:
   - Implement Drizzle ORM schema for users, accounts, ledger entries, transactions, splits, budgets, goals, debts, and holdings with Neon pooled connection.
3. **Phase 3: Universal Shell & Signature Pull-Down Switcher**:
   - Implement the 72dp pull-down gesture zone, haptics, and fallback pill switcher in `app/(tabs)/_layout.tsx`.
4. **Phase 4: 8-Step Onboarding Wizard**:
   - Build `app/(onboarding)/` multi-step flow seeding default Kenyan accounts and categories.
5. **Phase 5: Money Management Module**:
   - Implement Overview Dashboard, Transactions with splits and personal/business tags, Budgets with active rebalancing, Goals with emergency runway math, and Debt payoff advisor.
6. **Phase 6: Investment Management Module**:
   - Implement Portfolio Dashboard, Multi-Asset Holdings with isolated forex vs. capital returns, Activity Ledger, Projected Dividend Calendar, and Allocation/Risk charts.
7. **Phase 7: Hands-Free AI Voice Assistant ("Hey Numo")**:
   - Implement foreground wake-word detector, streaming assistant sheet, multi-turn memory, and Action Confirmation Preview modals.
8. **Phase 8: Sentry Observability, ImageKit Media, and CodeRabbit CI**:
   - Set up Sentry performance tracing, ImageKit signed token uploads, and GitHub Actions CI workflow.
