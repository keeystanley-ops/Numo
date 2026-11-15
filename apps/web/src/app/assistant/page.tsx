'use client';

import { useState } from 'react';
import Link from 'next/link';

interface ChatItem {
  role: 'user' | 'assistant';
  text: string;
  pendingAction?: {
    id: string;
    amount: number;
    description: string;
    account: string;
    category: string;
    currency: string;
    isIncome: boolean;
  } | null;
}

export default function AssistantPage() {
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<ChatItem[]>([]);

  async function sendMessage(e: React.FormEvent) {
    e.preventDefault();
    const q = input.trim();
    if (!q || loading) return;
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', text: q }]);
    setLoading(true);
    try {
      const res = await fetch('/api/hey-numo/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: q }),
      });
      const json = await res.json();
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', text: json.reply || 'No response', pendingAction: json.pendingAction },
      ]);
    } catch {
      setMessages((prev) => [...prev, { role: 'assistant', text: 'Failed to reach Hey Numo API.' }]);
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirm(action: NonNullable<ChatItem['pendingAction']>, confirmed: boolean) {
    await fetch('/api/hey-numo/confirm', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action, confirmed }),
    });
    setMessages((prev) => [
      ...prev,
      {
        role: 'assistant',
        text: confirmed
          ? `Confirmed! Recorded ${action.currency} ${action.amount} for ${action.description}.`
          : 'Cancelled — no changes were saved.',
      },
    ]);
  }

  return (
    <main style={{ maxWidth: 740, margin: '0 auto', padding: '32px 20px' }}>
      <Link href="/" style={{ fontSize: 13, color: '#007764', fontWeight: 700, textDecoration: 'none' }}>
        &larr; Back to Console
      </Link>
      <h1 style={{ fontSize: 28, fontWeight: 800, margin: '6px 0 18px' }}>Hey Numo Web Assistant</h1>

      <div className="numo-card" style={{ padding: 20, minHeight: 360, display: 'flex', flexDirection: 'column', gap: 12 }}>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 10 }}>
          {messages.length === 0 && (
            <p style={{ color: '#5c4636', fontSize: 14 }}>
              Ask about your available cash, Food budget, or try: &ldquo;Record KES 450 for lunch from M-PESA&rdquo;.
            </p>
          )}
          {messages.map((m, idx) => (
            <div
              key={idx}
              style={{
                alignSelf: m.role === 'user' ? 'flex-end' : 'flex-start',
                maxWidth: '82%',
                padding: '12px 16px',
                borderRadius: 16,
                background: m.role === 'user' ? '#007764' : '#faf4e8',
                color: m.role === 'user' ? '#fff' : '#2d1f14',
              }}
            >
              <div>{m.text}</div>
              {m.pendingAction && (
                <div style={{ marginTop: 10, padding: 12, borderRadius: 12, background: '#fff', border: '1px solid #e0911f' }}>
                  <div style={{ fontWeight: 700, fontSize: 13 }}>
                    Confirm: {m.pendingAction.currency} {m.pendingAction.amount} — {m.pendingAction.description}
                  </div>
                  <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                    <button className="numo-btn-primary" onClick={() => handleConfirm(m.pendingAction!, true)}>
                      Confirm
                    </button>
                    <button
                      onClick={() => handleConfirm(m.pendingAction!, false)}
                      style={{ padding: '10px 16px', borderRadius: 14, border: '1px solid #ccc', background: '#fff', cursor: 'pointer' }}
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
          {loading && <div style={{ fontSize: 13, color: '#007764' }}>Numo is thinking...</div>}
        </div>

        <form onSubmit={sendMessage} style={{ display: 'flex', gap: 10, marginTop: 12 }}>
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask Hey Numo anything..."
            style={{
              flex: 1,
              padding: '12px 16px',
              borderRadius: 14,
              border: '1px solid #dccfb8',
              background: '#fff',
              fontSize: 14,
            }}
          />
          <button type="submit" className="numo-btn-primary" disabled={loading}>
            Send
          </button>
        </form>
      </div>
    </main>
  );
}
