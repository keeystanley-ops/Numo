import type { Metadata } from 'next';
import { ClerkProvider } from '@clerk/nextjs';
import './globals.css';

export const metadata: Metadata = {
  title: 'Numo — Personal Finance & Wealth OS',
  description: 'Backend API & Universal Web Companion for Numo Personal Finance OS',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const pubKey = process.env.NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY || '';
  const clerkReady = Boolean(pubKey && !pubKey.includes('placeholder'));

  if (clerkReady) {
    return (
      <ClerkProvider>
        <html lang="en">
          <body>{children}</body>
        </html>
      </ClerkProvider>
    );
  }

  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
