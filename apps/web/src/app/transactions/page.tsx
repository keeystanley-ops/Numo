'use client';

import { Suspense, useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { Plus, Search, SlidersHorizontal, Trash2, X } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { formatKes, formatLocalDate } from '@/lib/currency';
import { DashboardPayload, useDashboardData } from '@/lib/useDashboard';

type TxRow = DashboardPayload['transactions'][number];

function TransactionsContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { data, loading, refresh } = useDashboardData();

  const [typeFilter, setTypeFilter] = useState(searchParams.get('type')?.toUpperCase() || 'ALL');
  const [search, setSearch] = useState(searchParams.get('q') || '');
  const [accountFilter, setAccountFilter] = useState(searchParams.get('account') || 'ALL');
  const [sort, setSort] = useState(searchParams.get('sort') || 'newest');
  const [showFilters, setShowFilters] = useState(false);

  const [selectedTx, setSelectedTx] = useState<TxRow | null>(null);
  const [editDesc, setEditDesc] = useState('');
  const [editAmt, setEditAmt] = useState('');
  const [busy, setBusy] = useState(false);

  const updateUrl = (nextType: string, nextQ: string, nextAcc: string, nextSort: string) => {
    const params = new URLSearchParams();
    if (nextType !== 'ALL') params.set('type', nextType);
    if (nextQ) params.set('q', nextQ);
    if (nextAcc !== 'ALL') params.set('account', nextAcc);
    if (nextSort !== 'newest') params.set('sort', nextSort);
    const qs = params.toString();
    router.replace(qs ? `/transactions?${qs}` : '/transactions', { scroll: false });
  };

  const transactions = data?.transactions || [];
  const accounts = data?.accounts || [];

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    let list = transactions.filter((t) => {
      if (typeFilter !== 'ALL' && t.type !== typeFilter) return false;
      if (accountFilter !== 'ALL' && t.sourceAccountId !== accountFilter && t.destinationAccountId !== accountFilter) {
        return false;
      }
      if (q) {
        const cats = (t.splits || []).map((s) => s.category).join(' ');
        const hay = `${t.description} ${t.merchant || ''} ${t.reference || ''} ${cats}`.toLowerCase();
        if (!hay.includes(q)) return false;
      }
      return true;
    });

    if (sort === 'oldest') list = [...list].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
    else if (sort === 'highest') list = [...list].sort((a, b) => Number(b.amount) - Number(a.amount));
    else if (sort === 'lowest') list = [...list].sort((a, b) => Number(a.amount) - Number(b.amount));
    else list = [...list].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
    return list;
  }, [transactions, typeFilter, accountFilter, search, sort]);

  const openDetail = (tx: TxRow) => {
    setSelectedTx(tx);
    setEditDesc(tx.description);
    setEditAmt(String(Number(tx.amount)));
  };

  const handleSaveEdit = async () => {
    if (!selectedTx || busy) return;
    setBusy(true);
    await fetch('/api/transactions', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id: selectedTx.id,
        description: editDesc,
        amount: Number(editAmt),
      }),
    });
    setBusy(false);
    setSelectedTx(null);
    refresh();
  };

  const handleDelete = async (id: string) => {
    if (busy) return;
    setBusy(true);
    await fetch(`/api/transactions?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
    setBusy(false);
    setSelectedTx(null);
    refresh();
  };

  const contextAddTab =
    typeFilter === 'INCOME' ? 'income' : typeFilter === 'TRANSFER' ? 'transfer' : 'expense';

  return (
    <NumoShell
      title="Transactions"
      backHref="/dashboard"
      rightSlot={
        <div style={{ display: 'flex', gap: 8 }}>
          <button
            onClick={() => setShowFilters(true)}
            aria-label="Filter and sort"
            className="numo-card"
            style={{ width: 36, height: 36, borderRadius: 12, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
          >
            <SlidersHorizontal size={17} />
          </button>
          <Link
            href={`/add-transaction?tab=${contextAddTab}`}
            aria-label="Add Transaction"
            style={{
              width: 36,
              height: 36,
              borderRadius: '50%',
              background: '#00796b',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              textDecoration: 'none',
            }}
          >
            <Plus size={18} />
          </Link>
        </div>
      }
    >
      {/* Search Bar */}
      <div className="numo-card" style={{ padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
        <Search size={17} color="#5c4636" />
        <input
          type="text"
          placeholder="Search transactions..."
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            updateUrl(typeFilter, e.target.value, accountFilter, sort);
          }}
          style={{ flex: 1, border: 'none', background: 'transparent', outline: 'none', fontSize: 14 }}
        />
        {search && (
          <button
            onClick={() => {
              setSearch('');
              updateUrl(typeFilter, '', accountFilter, sort);
            }}
            style={{ border: 'none', background: 'transparent', cursor: 'pointer' }}
          >
            <X size={16} />
          </button>
        )}
      </div>

      {/* Type Tabs */}
      <div style={{ display: 'flex', gap: 8, overflowX: 'auto', marginBottom: 14 }}>
        {[
          ['ALL', 'All'],
          ['INCOME', 'Income'],
          ['EXPENSE', 'Expenses'],
          ['TRANSFER', 'Transfers'],
        ].map(([val, lbl]) => (
          <button
            key={val}
            onClick={() => {
              setTypeFilter(val);
              updateUrl(val, search, accountFilter, sort);
            }}
            className="numo-pill"
            style={{
              background: typeFilter === val ? '#2d1f14' : '#f1e5cf',
              color: typeFilter === val ? '#fff' : '#2d1f14',
            }}
          >
            {lbl}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="numo-card" style={{ padding: 20 }}>Loading transactions...</div>
      ) : filtered.length === 0 ? (
        <div className="numo-card" style={{ padding: 24, textAlign: 'center' }}>
          <div style={{ fontWeight: 800 }}>No transactions found</div>
          <p style={{ fontSize: 12, color: '#5c4636', marginTop: 4 }}>Try clearing your filters or search query.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 10 }}>
          {filtered.map((tx) => (
            <div
              key={tx.id}
              onClick={() => openDetail(tx)}
              className="numo-card"
              style={{ padding: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }}
            >
              <div>
                <div style={{ fontWeight: 800, fontSize: 14 }}>{tx.description}</div>
                <div style={{ fontSize: 11, color: '#5c4636' }}>
                  {formatLocalDate(tx.date)} &bull; {tx.splits?.[0]?.category || tx.type}
                </div>
              </div>
              <strong
                style={{
                  fontSize: 14,
                  color: tx.type === 'INCOME' ? '#1b8a5a' : tx.type === 'TRANSFER' ? '#00796b' : '#d81b60',
                }}
              >
                {tx.type === 'INCOME' ? '+' : tx.type === 'EXPENSE' ? '-' : ''}
                {formatKes(Number(tx.amount))}
              </strong>
            </div>
          ))}
        </div>
      )}

      {/* Sort & Filter Modal */}
      {showFilters && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <h3 style={{ fontSize: 18, fontWeight: 800, marginBottom: 12 }}>Sort &amp; Filter</h3>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Sort By</label>
            <select className="numo-input" value={sort} onChange={(e) => setSort(e.target.value)}>
              <option value="newest">Newest First</option>
              <option value="oldest">Oldest First</option>
              <option value="highest">Highest Amount</option>
              <option value="lowest">Lowest Amount</option>
            </select>

            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginTop: 12, marginBottom: 4 }}>Account</label>
            <select className="numo-input" value={accountFilter} onChange={(e) => setAccountFilter(e.target.value)}>
              <option value="ALL">All Accounts</option>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>{a.name}</option>
              ))}
            </select>

            <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
              <button
                onClick={() => {
                  setSort('newest');
                  setAccountFilter('ALL');
                  updateUrl(typeFilter, search, 'ALL', 'newest');
                  setShowFilters(false);
                }}
                className="numo-card"
                style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}
              >
                Reset
              </button>
              <button
                onClick={() => {
                  updateUrl(typeFilter, search, accountFilter, sort);
                  setShowFilters(false);
                }}
                className="numo-btn-primary"
                style={{ flex: 1 }}
              >
                Apply
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Transaction Detail / Edit / Delete Modal */}
      {selectedTx && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 70 }}>
          <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <strong style={{ fontSize: 17 }}>Transaction Details</strong>
              <button onClick={() => setSelectedTx(null)} style={{ border: 'none', background: 'transparent', cursor: 'pointer' }}>
                <X size={18} />
              </button>
            </div>
            <div style={{ display: 'grid', gap: 10 }}>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Description</label>
                <input className="numo-input" value={editDesc} onChange={(e) => setEditDesc(e.target.value)} />
              </div>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Amount (KES)</label>
                <input type="number" className="numo-input" value={editAmt} onChange={(e) => setEditAmt(e.target.value)} />
              </div>
              {selectedTx.receiptUrl && (
                <div style={{ fontSize: 12, color: '#00796b' }}>
                  Receipt: {selectedTx.receiptUrl}
                </div>
              )}
              <div style={{ display: 'flex', gap: 10, marginTop: 6 }}>
                <button
                  onClick={() => handleDelete(selectedTx.id)}
                  disabled={busy}
                  className="numo-card"
                  style={{ padding: '10px 14px', color: '#d81b60', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}
                >
                  <Trash2 size={15} /> Delete
                </button>
                <button onClick={handleSaveEdit} disabled={busy} className="numo-btn-primary" style={{ flex: 1 }}>
                  {busy ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </NumoShell>
  );
}

export default function TransactionsPage() {
  return (
    <Suspense fallback={<div style={{ padding: 24 }}>Loading transactions...</div>}>
      <TransactionsContent />
    </Suspense>
  );
}
