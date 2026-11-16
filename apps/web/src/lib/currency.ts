/**
 * Global Numo Currency & Profile Greeting Utilities (KES Enforced)
 */

export const OPERATING_CURRENCY = 'KES' as const;

export function formatKes(amount: number | string | null | undefined, includeDecimals = false): string {
  const num = typeof amount === 'string' ? Number(amount) : Number(amount ?? 0);
  const safe = Number.isFinite(num) ? num : 0;
  const absVal = Math.abs(safe);
  const formatted = new Intl.NumberFormat('en-KE', {
    minimumFractionDigits: includeDecimals ? 2 : 0,
    maximumFractionDigits: includeDecimals ? 2 : 0,
  }).format(absVal);
  return `${safe < 0 ? '-' : ''}${OPERATING_CURRENCY} ${formatted}`;
}

export function extractFirstName(
  user?: {
    firstName?: string | null;
    fullName?: string | null;
    email?: string | null;
  } | null
): string | null {
  if (!user) return null;
  const candidate = (user.firstName || user.fullName || '').trim();
  if (candidate && candidate.toLowerCase() !== 'null' && candidate.toLowerCase() !== 'undefined') {
    const first = candidate.split(/\s+/)[0]?.trim();
    if (first && first.toLowerCase() !== 'null' && first.toLowerCase() !== 'undefined') {
      return first;
    }
  }
  const email = (user.email || '').trim();
  if (email.includes('@')) {
    const local = email.split('@')[0]?.trim();
    if (local && local.toLowerCase() !== 'null' && local.toLowerCase() !== 'undefined' && local.toLowerCase() !== 'user') {
      return local.charAt(0).toUpperCase() + local.slice(1);
    }
  }
  return null;
}

export function formatGreeting(
  user?: {
    firstName?: string | null;
    fullName?: string | null;
    email?: string | null;
  } | null,
  isLoading = false
): string {
  if (isLoading || !user) return 'Good Morning 👋';
  const first = extractFirstName(user);
  return first ? `Good Morning, ${first} 👋` : 'Good Morning 👋';
}

export function formatLocalDate(dateInput: string | Date): string {
  const d = typeof dateInput === 'string' ? new Date(dateInput) : dateInput;
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleDateString('en-KE', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}
