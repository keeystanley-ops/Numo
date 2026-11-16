'use client';

import { useState } from 'react';
import { Calculator, Delete, X } from 'lucide-react';
import { formatKes } from '@/lib/currency';

interface NumoCalculatorModalProps {
  initialAmount?: number;
  accentColor?: string;
  onClose: () => void;
  onApply: (value: number) => void;
}

export function evaluateCalculatorExpr(raw: string): { value: number; error: string | null } {
  const expr = raw.trim().replace(/×/g, '*').replace(/÷/g, '/').replace(/\s+/g, '');
  if (!expr) return { value: 0, error: null };
  const clean = /[+\-*/]$/.test(expr) ? expr.slice(0, -1) : expr;
  if (!clean) return { value: 0, error: null };

  try {
    let pos = 0;
    const peek = () => (pos < clean.length ? clean[pos] : null);

    const parseNumber = (): number => {
      const start = pos;
      if (peek() === '-') pos++;
      let dots = 0;
      while (pos < clean.length && (/\d/.test(clean[pos]) || clean[pos] === '.')) {
        if (clean[pos] === '.') dots++;
        if (dots > 1) throw new Error('Invalid number');
        pos++;
      }
      const sub = clean.slice(start, pos);
      const n = Number(sub);
      if (!sub || Number.isNaN(n)) throw new Error('Invalid syntax');
      return n;
    };

    const parseTerm = (): number => {
      let x = parseNumber();
      while (peek() === '*' || peek() === '/') {
        const op = clean[pos++];
        const y = parseNumber();
        if (op === '*') x *= y;
        else {
          if (Math.abs(y) < 1e-9) throw new Error('Cannot divide by zero');
          x /= y;
        }
      }
      return x;
    };

    const parseExpr = (): number => {
      let x = parseTerm();
      while (peek() === '+' || peek() === '-') {
        const op = clean[pos++];
        const y = parseTerm();
        x = op === '+' ? x + y : x - y;
      }
      return x;
    };

    const res = parseExpr();
    if (pos !== clean.length) throw new Error('Invalid expression');
    return { value: res, error: null };
  } catch (e) {
    return { value: 0, error: e instanceof Error ? e.message : 'Invalid expression' };
  }
}

export function NumoCalculatorModal({
  initialAmount = 0,
  accentColor = '#D81B60',
  onClose,
  onApply,
}: NumoCalculatorModalProps) {
  const [expression, setExpression] = useState(initialAmount > 0 ? String(initialAmount) : '');
  const [errorText, setErrorText] = useState<string | null>(null);

  const preview = evaluateCalculatorExpr(expression);

  const append = (token: string) => {
    setErrorText(null);
    const isOp = ['+', '-', '×', '÷'].includes(token);
    if (!expression && isOp) {
      if (token === '-') setExpression('-');
      return;
    }
    if (isOp && ['+', '-', '×', '÷'].includes(expression.slice(-1))) {
      setExpression(expression.slice(0, -1) + token);
      return;
    }
    if (token === '.') {
      const seg = expression.split(/[+\-×÷]/).pop() || '';
      if (seg.includes('.')) return;
      if (!seg) {
        setExpression(expression + '0.');
        return;
      }
    }
    setExpression(expression + token);
  };

  const handleApply = () => {
    if (!expression.trim()) {
      onApply(0);
      onClose();
      return;
    }
    const res = evaluateCalculatorExpr(expression);
    if (res.error || res.value < 0) {
      setErrorText(res.error || 'Result must be positive');
      return;
    }
    onApply(Math.round(res.value * 100) / 100);
    onClose();
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        background: 'rgba(11, 11, 11, 0.45)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 18,
        zIndex: 60,
      }}
    >
      <div className="numo-card" style={{ width: '100%', maxWidth: 380, padding: 20, background: '#fef9ef' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div
              style={{
                width: 34,
                height: 34,
                borderRadius: 10,
                background: 'rgba(245, 158, 11, 0.18)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#d97706',
              }}
            >
              <Calculator size={18} />
            </div>
            <strong style={{ fontSize: 17 }}>Calculator</strong>
          </div>
          <button
            onClick={onClose}
            aria-label="Close calculator"
            style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#5c4636' }}
          >
            <X size={20} />
          </button>
        </div>

        <div className="numo-card" style={{ padding: '14px 16px', textAlign: 'right', marginBottom: 14 }}>
          <div style={{ fontSize: 15, color: '#5c4636', minHeight: 20 }}>{expression || '0'}</div>
          <div
            style={{
              fontSize: errorText || preview.error ? 15 : 26,
              fontWeight: 800,
              color: errorText || preview.error ? '#d81b60' : '#2d1f14',
              marginTop: 4,
            }}
          >
            {errorText || preview.error || formatKes(preview.value)}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10 }}>
          <button
            onClick={() => {
              setExpression('');
              setErrorText(null);
            }}
            style={calcBtnStyle('rgba(216, 27, 96, 0.12)', '#d81b60')}
          >
            C
          </button>
          <button onClick={() => append('÷')} style={calcBtnStyle('#f3e7d3', '#2d1f14')}>÷</button>
          <button onClick={() => append('×')} style={calcBtnStyle('#f3e7d3', '#2d1f14')}>×</button>
          <button
            onClick={() => setExpression((e) => e.slice(0, -1))}
            style={calcBtnStyle('#f3e7d3', '#2d1f14')}
            aria-label="Backspace"
          >
            <Delete size={18} />
          </button>

          {['7', '8', '9'].map((d) => (
            <button key={d} onClick={() => append(d)} style={calcBtnStyle('#fff', '#2d1f14')}>
              {d}
            </button>
          ))}
          <button onClick={() => append('-')} style={calcBtnStyle('#f3e7d3', '#2d1f14')}>-</button>

          {['4', '5', '6'].map((d) => (
            <button key={d} onClick={() => append(d)} style={calcBtnStyle('#fff', '#2d1f14')}>
              {d}
            </button>
          ))}
          <button onClick={() => append('+')} style={calcBtnStyle('#f3e7d3', '#2d1f14')}>+</button>

          {['1', '2', '3'].map((d) => (
            <button key={d} onClick={() => append(d)} style={calcBtnStyle('#fff', '#2d1f14')}>
              {d}
            </button>
          ))}
          <button
            onClick={handleApply}
            style={{
              ...calcBtnStyle(accentColor, '#fff'),
              gridRow: 'span 2',
              height: '100%',
              flexDirection: 'column',
              gap: 2,
            }}
          >
            <span style={{ fontSize: 22 }}>=</span>
            <span style={{ fontSize: 11 }}>Done</span>
          </button>

          <button onClick={() => append('0')} style={{ ...calcBtnStyle('#fff', '#2d1f14'), gridColumn: 'span 2' }}>
            0
          </button>
          <button onClick={() => append('.')} style={calcBtnStyle('#fff', '#2d1f14')}>.</button>
        </div>
      </div>
    </div>
  );
}

function calcBtnStyle(bg: string, color: string): React.CSSProperties {
  return {
    height: 52,
    borderRadius: 16,
    border: 'none',
    background: bg,
    color,
    fontSize: 19,
    fontWeight: 700,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    boxShadow: '0 4px 10px rgba(91, 58, 41, 0.08)',
  };
}
