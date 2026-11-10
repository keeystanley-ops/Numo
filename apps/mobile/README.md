# Numo Mobile & Web Application (`apps/mobile`)

**Numo** is a personal finance and wealth management operating system built with **Expo Router** supporting iOS, Android, and Web from a single codebase.

## Prerequisites

- Node.js >= 18
- npm or pnpm or yarn
- Expo CLI (`npx expo`)

## Setup & Getting Started

1. Navigate to this directory:
   ```bash
   cd apps/mobile
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure environment variables:
   All keys are already configured in `.env` and `.env.local`.

4. Run the development server:
   - **With Development Client (`expo-dev-client`)**:
     ```bash
     npm run dev
     # or: npx expo start --dev-client
     ```
   - **For Native Dev Builds**:
     ```bash
     npm run android:dev   # npx expo run:android
     npm run ios:dev       # npx expo run:ios
     ```
   - **Create Cloud Dev Build with EAS**:
     ```bash
     npx eas build --profile development --platform all
     ```
   - **For Web Browser**:
     ```bash
     npm run web           # npx expo start --web
     ```

## Database Migrations (Drizzle ORM & Neon)

To apply the double-entry schema to your Neon database:
```bash
npx drizzle-kit push
```

To explore data interactively via Drizzle Studio:
```bash
npx drizzle-kit studio
```
