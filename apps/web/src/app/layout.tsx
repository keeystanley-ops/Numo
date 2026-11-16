import type { Metadata } from 'next';
import { Plus_Jakarta_Sans } from 'next/font/google';
import { ClerkProvider } from '@clerk/nextjs';
import './globals.css';

const plusJakarta = Plus_Jakarta_Sans({
  subsets: ['latin'],
  weight: ['400', '500', '600', '700', '800'],
  variable: '--font-plus-jakarta',
});

export const metadata: Metadata = {
  title: 'Numo — Personal Finance & Wealth OS',
  description: 'Numo Personal Finance Operating System — KES Ledger, Budgets, Goals, Investments & Hey Numo AI',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const pubKey = process.env.NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY || '';
  const clerkReady = Boolean(pubKey && !pubKey.includes('placeholder'));

  if (clerkReady) {
    return (
      <ClerkProvider>
        <html lang="en" className={plusJakarta.variable}>
          <body style={{ fontFamily: 'var(--font-plus-jakarta), sans-serif' }}>{children}</body>
        </html>
      </ClerkProvider>
    );
  }

  return (
    <html lang="en" className={plusJakarta.variable}>
      <body style={{ fontFamily: 'var(--font-plus-jakarta), sans-serif' }}>{children}</body>
    </html>
  );
}
