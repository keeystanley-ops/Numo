'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Bell, ChevronRight, HelpCircle, Landmark, LogOut, Settings, Shield, Tag, User } from 'lucide-react';
import { NumoShell } from '@/components/NumoShell';
import { useDashboardData } from '@/lib/useDashboard';

export default function ProfilePage() {
  const { data, refresh } = useDashboardData();
  const [subModal, setSubModal] = useState<'PERSONAL' | 'HELP' | 'CATEGORIES' | 'LOGOUT' | null>(null);
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [newCat, setNewCat] = useState('');
  const [signingOut, setSigningOut] = useState(false);

  const handleSignOut = async () => {
    if (signingOut) return;
    setSigningOut(true);
    try {
      await fetch('/api/auth/sign-out', { method: 'POST' });
      const clerkGlobal = (window as unknown as { Clerk?: { signOut?: () => Promise<void> } }).Clerk;
      if (clerkGlobal?.signOut) {
        await clerkGlobal.signOut().catch(() => {});
      }
      if (typeof window !== 'undefined') {
        window.sessionStorage.clear();
      }
    } finally {
      window.location.assign('/sign-in');
    }
  };

  const openPersonal = () => {
    setFullName(data?.user?.fullName || '');
    setPhone(data?.user?.phone || '');
    setSubModal('PERSONAL');
  };

  const savePersonal = async (e: React.FormEvent) => {
    e.preventDefault();
    await fetch('/api/profile', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, phone }),
    });
    setSubModal(null);
    refresh();
  };

  const addCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCat.trim()) return;
    await fetch('/api/categories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: newCat.trim(), type: 'EXPENSE' }),
    });
    setNewCat('');
    refresh();
  };

  const displayName = data?.user?.fullName || data?.user?.firstName || 'Numo Member';

  return (
    <NumoShell
      title="Profile"
      backHref="/dashboard"
      rightSlot={
        <Link href="/settings" aria-label="Settings" style={{ color: '#2d1f14' }}>
          <Settings size={20} />
        </Link>
      }
    >
      <div className="numo-card" style={{ padding: 20, textAlign: 'center', marginBottom: 16 }}>
        <div
          style={{
            width: 68,
            height: 68,
            borderRadius: '50%',
            margin: '0 auto 10px',
            background: 'linear-gradient(135deg,#14b8a6,#00796b)',
            color: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 26,
            fontWeight: 800,
          }}
        >
          {displayName.charAt(0).toUpperCase()}
        </div>
        <h2 style={{ fontSize: 18, fontWeight: 800 }}>{displayName}</h2>
        <div style={{ fontSize: 12, color: '#5c4636' }}>{data?.user?.email || 'Member'} &bull; KES</div>
      </div>

      <div className="numo-card" style={{ padding: 8, display: 'grid', gap: 4 }}>
        <MenuRow icon={<User size={18} />} label="Personal Information" onClick={openPersonal} />
        <Link href="/accounts" style={{ textDecoration: 'none', color: 'inherit' }}>
          <MenuRow icon={<Landmark size={18} />} label="Linked Accounts" />
        </Link>
        <Link href="/settings" style={{ textDecoration: 'none', color: 'inherit' }}>
          <MenuRow icon={<Shield size={18} />} label="Security & Privacy" />
        </Link>
        <Link href="/settings" style={{ textDecoration: 'none', color: 'inherit' }}>
          <MenuRow icon={<Bell size={18} />} label="Notifications" />
        </Link>
        <MenuRow icon={<Tag size={18} />} label="Manage Categories" onClick={() => setSubModal('CATEGORIES')} />
        <MenuRow icon={<HelpCircle size={18} />} label="Help & Support" onClick={() => setSubModal('HELP')} />
        <Link href="/settings" style={{ textDecoration: 'none', color: 'inherit' }}>
          <MenuRow icon={<Settings size={18} />} label="Settings & Voice" />
        </Link>
      </div>

      {/* Dedicated Sign Out Button on Profile Page */}
      <button
        type="button"
        onClick={() => setSubModal('LOGOUT')}
        disabled={signingOut}
        className="numo-card"
        style={{
          width: '100%',
          marginTop: 16,
          padding: '14px 18px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 10,
          color: '#d81b60',
          fontSize: 15,
          fontWeight: 800,
          cursor: 'pointer',
          border: '1.5px solid rgba(216, 27, 96, 0.25)',
          background: 'rgba(216, 27, 96, 0.06)',
        }}
      >
        <LogOut size={18} color="#d81b60" />
        <span>{signingOut ? 'Signing Out...' : 'Sign Out'}</span>
      </button>

      {subModal === 'PERSONAL' && (
        <ModalBox title="Personal Information" onClose={() => setSubModal(null)}>
          <form onSubmit={savePersonal} style={{ display: 'grid', gap: 10 }}>
            <input className="numo-input" placeholder="Full Name" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
            <input className="numo-input" placeholder="Phone (+254...)" value={phone} onChange={(e) => setPhone(e.target.value)} />
            <button type="submit" className="numo-btn-primary">Save Changes</button>
          </form>
        </ModalBox>
      )}

      {subModal === 'CATEGORIES' && (
        <ModalBox title="Custom Categories" onClose={() => setSubModal(null)}>
          <form onSubmit={addCategory} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
            <input className="numo-input" placeholder="New category" value={newCat} onChange={(e) => setNewCat(e.target.value)} />
            <button type="submit" className="numo-btn-primary">Add</button>
          </form>
          <div style={{ display: 'grid', gap: 6 }}>
            {(data?.categories || []).map((c) => (
              <div key={c.id} className="numo-card" style={{ padding: '8px 12px', fontSize: 13, fontWeight: 700 }}>
                {c.name}
              </div>
            ))}
          </div>
        </ModalBox>
      )}

      {subModal === 'HELP' && (
        <ModalBox title="Help & Support" onClose={() => setSubModal(null)}>
          <div style={{ display: 'grid', gap: 10, fontSize: 13 }}>
            <div className="numo-card" style={{ padding: 12 }}>
              <strong>How does Available Cash work?</strong>
              <p style={{ color: '#5c4636', marginTop: 4 }}>Available Cash = Total Active Connected Accounts minus Reserved Unpaid Bills.</p>
            </div>
            <div className="numo-card" style={{ padding: 12 }}>
              <strong>Contact Support</strong>
              <p style={{ color: '#5c4636', marginTop: 4 }}>Email: support@numofinance.co.ke</p>
            </div>
          </div>
        </ModalBox>
      )}

      {subModal === 'LOGOUT' && (
        <ModalBox title="Sign Out" onClose={() => setSubModal(null)}>
          <p style={{ fontSize: 13, color: '#5c4636', marginBottom: 14 }}>Are you sure you want to sign out of Numo?</p>
          <div style={{ display: 'flex', gap: 10 }}>
            <button onClick={() => setSubModal(null)} className="numo-card" style={{ flex: 1, padding: 12, fontWeight: 700, cursor: 'pointer' }}>Cancel</button>
            <button
              type="button"
              onClick={handleSignOut}
              disabled={signingOut}
              className="numo-btn-primary"
              style={{ flex: 1, background: '#d81b60' }}
            >
              {signingOut ? 'Signing Out...' : 'Sign Out'}
            </button>
          </div>
        </ModalBox>
      )}
    </NumoShell>
  );
}

function MenuRow({
  icon,
  label,
  danger,
  onClick,
}: {
  icon: React.ReactNode;
  label: string;
  danger?: boolean;
  onClick?: () => void;
}) {
  return (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '12px 14px',
        borderRadius: 14,
        cursor: 'pointer',
        color: danger ? '#d81b60' : '#2d1f14',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        {icon}
        <span style={{ fontSize: 14, fontWeight: 700 }}>{label}</span>
      </div>
      <ChevronRight size={16} color="#5c4636" />
    </div>
  );
}

function ModalBox({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(11,11,11,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 18, zIndex: 60 }}>
      <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <strong style={{ fontSize: 17 }}>{title}</strong>
          <button onClick={onClose} style={{ border: 'none', background: 'transparent', cursor: 'pointer', fontWeight: 700 }}>Close</button>
        </div>
        {children}
      </div>
    </div>
  );
}
