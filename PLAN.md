# Numo — Personal Finance & Wealth Operating System

> **Comprehensive Architecture & Implementation Specification (Universal Expo Router)**

**Numo** is a privacy-first personal finance and wealth management application uniting two distinct yet connected environments: **Money Management** (daily income, expenses, budgets, savings goals, debt, and cash flow) and **Investment Management** (multi-asset portfolios, holdings, dividends, interest, performance, and asset allocation).

Both environments share a unified financial engine, a strict double-entry ledger, and a hands-free conversational AI assistant (**"Hey Numo"**). The application is scaffolded inside **`apps/mobile`** using **Expo Managed Workflow** with **Expo Router universal multi-platform support** (powering iOS, Android, and Web from a single unified codebase), backed by a serverless **tRPC API (Next.js / Hono)**, **Neon PostgreSQL** with **Drizzle ORM**, **Clerk** authentication + on-device biometrics, **ImageKit.io** media optimization, **Sentry** full-stack telemetry, and **CodeRabbit** automated code review.

---

## Quick Overview & Technology Stack

| Layer | Technology / Choice | Purpose & Notes |
| :--- | :--- | :--- |
| **Product Identity** | **Numo** | Kenya-first, globally expandable personal finance & wealth OS |
| **Workspace** | `apps/mobile` (Expo Managed + Expo Router) | Single universal codebase for **iOS, Android, and Web** (`"web": { "bundler": "metro" }`) |
| **Backend API** | Serverless **Next.js / Hono** + **tRPC** | End-to-end type-safe API routers (`auth`, `accounts`, `transactions`, `budgets`, `investments`, `assistant`) |
| **Database & ORM** | **Neon PostgreSQL** + **Drizzle ORM** | Strict double-entry immutable ledger; pooled (`DATABASE_URL`) & unpooled (`DATABASE_URL_UNPOOLED`) |
| **Neon Project** | `old-bar-99839974` (`production` branch) | Configured with Neon coding agent skills (`.agents/skills/neon*`) |
| **Authentication** | **Clerk** + **Expo Local Authentication** | Mandatory cloud sign-in on first launch; Face ID / Touch ID / device PIN for quick unlocks |
| **Media Pipeline** | **ImageKit.io** | Direct client receipt & document uploads via backend-signed HMAC tokens |
| **Voice Assistant** | **"Hey Numo"** (Hands-Free AI) | In-app wake-word detection, multi-turn memory, proactive follow-ups, mandatory confirmation modal |
| **Observability & CI** | **Sentry** + **GitHub Actions** + **CodeRabbit** | Crash reporting, distributed tracing, session replay, PR reviews & Drizzle migration checks |
| **Offline Resilience** | **TanStack Query (React Query)** Cache | Read-only local cache with an informative offline banner when connectivity drops |

### Contents

- [User Review & Critical Decisions](#user-review--critical-decisions)
- [1. Directory Structure (`apps/mobile`)](#1-directory-structure-appsmobile)
- [2. System Architecture & Component Diagram](#2-system-architecture--component-diagram)
- [3. The 8-Step First-Use Onboarding Sequence](#3-the-8-step-first-use-onboarding-sequence)
- [4. Screen-by-Screen Layout & Component Breakdown](#4-screen-by-screen-layout--component-breakdown)
- [5. Strict Double-Entry Ledger & Financial Accounting Rules](#5-strict-double-entry-ledger--financial-accounting-rules)
- [6. The 10 Practical Acceptance Test Scenarios](#6-the-10-practical-acceptance-test-scenarios)
- [7. Phased Implementation Roadmap](#7-phased-implementation-roadmap)

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions are established and locked in:
>
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

### Module Responsibilities

| Path | Responsibility |
| :--- | :--- |
| `apps/mobile/app/` | Universal Expo Router file-based routes for auth, onboarding, dual-environment tabs, modals, and assistant |
| `apps/mobile/components/` | Reusable UI primitives (`ui/`), data visualizations (`charts/`), and tabular accounting rows (`ledger/`) |
| `apps/mobile/lib/` | tRPC client, ImageKit HMAC uploader, double-entry validation engine, and "Hey Numo" voice pipeline |
| `apps/mobile/db/` | Drizzle ORM PostgreSQL schema definitions and pooled Neon database client |

### Route & File Tree

```text
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

```text
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

| Step | Screen | Key Capabilities & Guardrails |
| :---: | :--- | :--- |
| **1** | **Welcome & Clerk Authentication** | Welcome hero introducing Numo's dual-environment OS; Clerk auth via Email magic link, SMS OTP, or Google/Apple sign-in; syncs user entity into Neon Postgres. |
| **2** | **Biometric & Local PIN Security** | Configure Face ID, Touch ID, or device PIN via `expo-local-authentication` (*"Secure your financial records so only you can unlock the app on this device."*). |
| **3** | **Financial Localization & Settings** | Country selector (default: **Kenya**), primary currency (default: **KES**, plus USD, EUR, GBP, TZS, UGX), `DD/MM/YYYY` formatting, and hands-free **"Hey Numo"** mic permission. |
| **4** | **Setup Money Accounts** | Quick-add **Cash on Hand**, **M-PESA Wallet**, **Bank Accounts** (Equity, KCB, Standard Chartered, Absa), and **Savings**. *Starting balances represent existing assets and do NOT inflate monthly income.* |
| **5** | **Initial Budget Creation** | Select Category Limits, Flexible Budget, or Zero-Based Budgeting with pre-populated Kenyan categories: Rent, Food, Matatu/Fuel, KPLC/Water, Airtime/Data, Entertainment, Healthcare (SHA). |
| **6** | **Savings Goals & Debt** *(Optional)* | Configure an **Emergency Fund Runway** (auto-calculates 3, 6, or 9 months of essential expenses) and optionally log existing liabilities (HELB student loans, bank loans, mobile loans). |
| **7** | **Setup Investment Portfolio** *(Optional)* | Create investment accounts (NSE Brokerage, MMF e.g. Sanlam/Britam/CIC, SACCO e.g. Stima/Harambee, Treasury Bills/Bonds) and log initial holdings or invested capital. |
| **8** | **Enter Preferred Environment** | Choose initial landing environment (**Money Management** or **Investment Management**) with a smooth animated transition directly into the live dashboard. |

---

## 4. Screen-by-Screen Layout & Component Breakdown

### 4.1 Signature Interaction: Pull-Down Environment Switcher

- **Dedicated Interaction Zone**: `48dp` zone anchored beneath the system status bar.
- **Gesture Dynamics**: Vertical drag exceeding `72dp` triggers haptic feedback (`Haptics.impactAsync(ImpactFeedbackStyle.Medium)`) and activates a smooth shared-element morphing transition swapping the active environment.
- **Scroll Conflict Isolation**: Vertical scrolling inside nested lists (transactions, holdings) never accidentally triggers the switcher.
- **Accessibility Fallback**: Top app bar Environment Pill Switcher (`[Money | Investments]`) provides instant one-tap switching.
- **State Preservation**: Switching preserves each environment's internal navigation stack, scroll offset, and filter states.

### 4.2 Money Management Environment

| Screen | Core Purpose & Features |
| :--- | :--- |
| **1. Overview (Dashboard)** | Available Cash card, monthly cash flow gauge (Income vs. Spent vs. Saved), budget health meters, upcoming obligations reel, and recent activity feed. |
| **2. Transactions** | Search and multi-filter bar, date-grouped list with category avatars and M-PESA chips, and transaction sheet with **Multi-Category Splitting** and **Personal vs. Business Tagging** per line. |
| **3. Budgets** | Category limits, flexible budgets, and zero-based budgeting with pay-cycle support (e.g., 25th payday cycle) and an **Active Overspending Rebalancer** prompting fund shifts when categories exceed limits. |
| **4. Goals & Debt** | Savings goal cards with circular progress, dynamic emergency fund runway (3, 6, 9 months of essential spend), and an **AI Debt Payoff Advisor** comparing Avalanche and Snowball repayment roadmaps. |
| **5. Insights** | Interactive SVG donut chart with transaction drill-down, top merchant leaderboard, 50/30/20 needs vs. wants ratio, and monthly cash flow history. |

### 4.3 Investment Management Environment

| Screen | Core Purpose & Features |
| :--- | :--- |
| **1. Portfolio Dashboard** | Total portfolio valuation, invested capital, total return %, valuation freshness badges (*"Valued 2 days ago"*), and upcoming 30-day dividend strip. |
| **2. Holdings** | Directory for Stocks (NSE & Global), MMF, Treasury Bills, Bonds, SACCO, REITs, Crypto, and Real Estate. Foreign assets isolate **USD asset price return** from **KES foreign exchange currency movement**. |
| **3. Activity Ledger** | Filterable transaction log of buys, sells, deposits, withdrawals, dividend distributions, fees, and withholding tax deductions. |
| **4. Income & Dividends** | Monthly, quarterly, and annual investment income run-rate paired with an income calendar plotting confirmed receipts and future estimated dates projected from entered dividend yields. |
| **5. Analysis & Risk** | Asset class allocation donut and portfolio concentration alerts (*"Top 3 holdings represent 64% of portfolio"*). |

### 4.4 Conversational Voice Assistant ("Hey Numo")

- **Hands-Free Activation**: In-app continuous keyword detection listening for **"Hey Numo"** while the app is in the foreground — no physical microphone button required.
- **Assistant Sheet (Overlay Modal)**:
  - Ambient listening animation with real-time voice transcription.
  - **Multi-Turn Session Memory**: Retains conversation context across exchanges.
  - **Proactive Follow-Ups**: Prompts for missing fields when user instructions are ambiguous.
  - **Action Confirmation Card**: Displays an explicit preview card (Amount, Category, Account, Date) before any database write and never commits without tapping **Confirm**.

---

## 5. Strict Double-Entry Ledger & Financial Accounting Rules

| # | Accounting Rule | Ledger & Reporting Enforcement |
| :---: | :--- | :--- |
| **1** | **Transfers are NEVER Income or Expenses** | Moving funds between accounts (e.g., Bank → M-PESA) creates paired debit/credit entries leaving liquid cash and income/expense totals unchanged. |
| **2** | **Investment Contributions are NOT Ordinary Expenses** | Moving cash to an investment account debits the asset holding and credits cash, increasing invested capital without distorting living expenses. |
| **3** | **Debt Payments Separate Principal and Interest** | Repayments strictly separate liability balance reduction (principal debit) from living expense (interest fee debit). |
| **4** | **Dividends & Interest Classified as Investment Income** | Received income credits Investment Income and debits cash without inflating asset cost basis or distorting net worth. |
| **5** | **Asset Valuation Changes are NOT Cash Income** | Property and stock adjustments record unrealized gains/losses and are never counted as liquid cash. |
| **6** | **Sale Proceeds Distinguished from Capital Gains** | Asset liquidations distinguish gross proceeds, acquisition cost basis, fees, and net realized gain/loss. |
| **7** | **Currency Integrity** | Foreign holdings store original currency values and exchange rates, displaying KES equivalents without overwriting original records. |
| **8** | **Immutable Audit Trail** | All adjustments append audit records with timestamps rather than silently mutating historical reports. |

---

## 6. The 10 Practical Acceptance Test Scenarios

| # | Scenario | User Action | Verified Expected Outcome |
| :---: | :--- | :--- | :--- |
| **1** | **First-Time User** | Signs up via Clerk, sets PIN, selects Kenya & KES, inputs KES 50,000 cash balance, creates food budget, opens Investments. | Starting balance credited to Cash without generating artificial monthly income. |
| **2** | **Record an Expense** | Records KES 500 Food expense from M-PESA. | M-PESA decreases by 500, Food budget spent increases by 500, ledger records Debit: Food / Credit: M-PESA. |
| **3** | **Transfer Money** | Transfers KES 2,000 from Bank to M-PESA. | Bank decreases by 2,000, M-PESA increases by 2,000; cash assets and living expenses remain unchanged. |
| **4** | **Invest Money** | Moves KES 5,000 from Bank into Investment Account for NSE shares. | Cash decreases by 5,000, investment holding increases by 5,000; total net worth preserved. |
| **5** | **Receive a Dividend** | Records KES 1,000 dividend deposited into Cash. | Cash increases by 1,000, Investment Income records 1,000, Net Worth increases by 1,000 with zero double-counting. |
| **6** | **Pay Debt** | Pays KES 3,000 on loan (KES 2,500 principal + KES 500 interest). | Debt decreases by 2,500, interest expense logs 500, cash decreases by 3,000. |
| **7** | **Use Voice Assistant** | Speaks *"Hey Numo, record 600 shillings for transport"*. | Assistant activates hands-free, presents Action Confirmation Modal, and commits only upon tapping Confirm. |
| **8** | **Switch Environments** | Drags down `72dp` from top interaction zone in Money Management. | UI morphs into Investment Management with haptic feedback; scrolling lists never triggers switching. |
| **9** | **Offline Operation** | Toggles Airplane Mode. | App transitions to read-only mode, displays offline status banner, and shows cached balances without crashing. |
| **10** | **Recover Financial Data** | Logs in on a secondary device via Clerk. | Neon Postgres streams complete historical records; all data verified identical with zero duplicates. |

---

## 7. Phased Implementation Roadmap

| Phase | Milestone | Key Deliverables & Files |
| :---: | :--- | :--- |
| **Phase 1** | **Project Scaffolding in `apps/mobile`** | Initialize Expo project with Expo Router, TypeScript, and universal web support (`"web": { "bundler": "metro" }`); configure `@clerk/clerk-expo`, `expo-local-authentication`, `@sentry/react-native`, and `imagekit-javascript`. |
| **Phase 2** | **Database Schema & Double-Entry Ledger** | Implement Drizzle ORM schema in `db/schema.ts` for users, accounts, ledger entries, transactions, splits, budgets, goals, debts, and holdings with Neon pooled connection. |
| **Phase 3** | **Universal Shell & Pull-Down Switcher** | Implement the `72dp` pull-down gesture zone, haptics, and fallback pill switcher in `app/(tabs)/_layout.tsx`. |
| **Phase 4** | **8-Step Onboarding Wizard** | Build `app/(onboarding)/` multi-step flow seeding default Kenyan accounts and spending categories. |
| **Phase 5** | **Money Management Module** | Implement Overview Dashboard, Transactions with splits and personal/business tags, Budgets with active rebalancing, Goals with emergency runway math, and Debt payoff advisor. |
| **Phase 6** | **Investment Management Module** | Implement Portfolio Dashboard, Multi-Asset Holdings with isolated forex vs. capital returns, Activity Ledger, Projected Dividend Calendar, and Allocation/Risk charts. |
| **Phase 7** | **Hands-Free AI Voice Assistant ("Hey Numo")** | Implement foreground wake-word detector, streaming assistant sheet, multi-turn memory, and Action Confirmation Preview modals. |
| **Phase 8** | **Sentry Observability, ImageKit & CodeRabbit CI** | Set up Sentry performance tracing, ImageKit signed token uploads, and GitHub Actions CI workflow with automated Drizzle migration verification. |
