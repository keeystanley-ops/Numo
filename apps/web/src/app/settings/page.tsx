'use client';

import { useEffect, useState } from 'react';
import { NumoShell } from '@/components/NumoShell';
import { useDashboardData } from '@/lib/useDashboard';

export default function SettingsPage() {
  const { data, refresh } = useDashboardData();
  const [voiceSpeed, setVoiceSpeed] = useState('Normal');
  const [voiceName, setVoiceName] = useState('Sulafat');
  const [reminders, setReminders] = useState(true);
  const [budgetAlerts, setBudgetAlerts] = useState(true);
  const [biometric, setBiometric] = useState(false);
  const [savedMsg, setSavedMsg] = useState<string | null>(null);

  useEffect(() => {
    if (data?.user) {
      setVoiceSpeed(data.user.voiceSpeed || 'Normal');
      setVoiceName(data.user.voiceName || 'Sulafat');
      setReminders(data.user.reminderNotifications ?? true);
      setBudgetAlerts(data.user.budgetAlertNotifications ?? true);
      setBiometric(data.user.biometricEnabled ?? false);
    }
  }, [data]);

  const handleSave = async () => {
    await fetch('/api/profile', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        voiceSpeed,
        voiceName,
        reminderNotifications: reminders,
        budgetAlertNotifications: budgetAlerts,
        biometricEnabled: biometric,
      }),
    });
    setSavedMsg('Settings saved to database.');
    refresh();
  };

  return (
    <NumoShell title="Settings" backHref="/profile">
      <div style={{ display: 'grid', gap: 14 }}>
        <div className="numo-card" style={{ padding: 16 }}>
          <h2 style={{ fontSize: 15, fontWeight: 800, marginBottom: 10 }}>Operating Currency</h2>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#00796b' }}>
            🇰🇪 Kenyan Shilling (KES) — Enforced App-Wide
          </div>
        </div>

        <div className="numo-card" style={{ padding: 16, display: 'grid', gap: 10 }}>
          <h2 style={{ fontSize: 15, fontWeight: 800 }}>Hey Numo Voice Assistant</h2>
          <div>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Voice Selection</label>
            <select className="numo-input" value={voiceName} onChange={(e) => setVoiceName(e.target.value)}>
              <option value="Sulafat">Sulafat</option>
              <option value="Aoede">Aoede</option>
              <option value="Kore">Kore</option>
            </select>
          </div>
          <div>
            <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Speech Speed</label>
            <select className="numo-input" value={voiceSpeed} onChange={(e) => setVoiceSpeed(e.target.value)}>
              <option value="Slow">Slow</option>
              <option value="Normal">Normal</option>
              <option value="Fast">Fast</option>
            </select>
          </div>
        </div>

        <div className="numo-card" style={{ padding: 16, display: 'grid', gap: 10 }}>
          <h2 style={{ fontSize: 15, fontWeight: 800 }}>Notifications &amp; Security</h2>
          <label style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, fontWeight: 600 }}>
            <span>Bill Due Reminders</span>
            <input type="checkbox" checked={reminders} onChange={(e) => setReminders(e.target.checked)} />
          </label>
          <label style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, fontWeight: 600 }}>
            <span>Budget Threshold Alerts</span>
            <input type="checkbox" checked={budgetAlerts} onChange={(e) => setBudgetAlerts(e.target.checked)} />
          </label>
          <label style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, fontWeight: 600 }}>
            <span>Biometric Unlock Preference</span>
            <input type="checkbox" checked={biometric} onChange={(e) => setBiometric(e.target.checked)} />
          </label>
        </div>

        {savedMsg && (
          <div style={{ padding: 10, borderRadius: 12, background: 'rgba(27,138,90,0.14)', color: '#1b8a5a', fontSize: 13, fontWeight: 700 }}>
            {savedMsg}
          </div>
        )}

        <button onClick={handleSave} className="numo-btn-primary" style={{ width: '100%' }}>
          Save Settings
        </button>
      </div>
    </NumoShell>
  );
}
