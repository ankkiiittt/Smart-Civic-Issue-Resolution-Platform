import React from 'react';
import { AlertTriangle, Bell, X } from 'lucide-react';

export interface ToastItem {
  id: string;
  message: string;
  type: 'UPDATE' | 'ESCALATION';
}

interface ToastListProps {
  toasts: ToastItem[];
  onRemove: (id: string) => void;
}

export const ToastList: React.FC<ToastListProps> = ({ toasts, onRemove }) => {
  return (
    <div className="toast-container">
      {toasts.map((t) => {
        const isEscalation = t.type === 'ESCALATION';

        return (
          <div
            key={t.id}
            className="glass-panel toast"
            style={{
              borderLeft: `4px solid ${isEscalation ? 'var(--accent-danger)' : 'var(--accent-primary)'}`,
              background: 'rgba(15, 23, 42, 0.95)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              minWidth: '280px',
              maxWidth: '380px',
              padding: '16px',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', textAlign: 'left' }}>
              {isEscalation ? (
                <AlertTriangle size={20} style={{ color: 'var(--accent-danger)', flexShrink: 0 }} />
              ) : (
                <Bell size={20} style={{ color: 'var(--accent-primary)', flexShrink: 0 }} />
              )}
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '12px', fontWeight: 700, color: isEscalation ? 'var(--accent-danger)' : 'var(--accent-primary)' }}>
                  {isEscalation ? 'SLA BREACH ALERT' : 'TICKET UPDATE'}
                </span>
                <span style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
                  {t.message}
                </span>
              </div>
            </div>

            <button
              onClick={() => onRemove(t.id)}
              style={{
                background: 'none',
                border: 'none',
                color: 'var(--text-secondary)',
                cursor: 'pointer',
                padding: '4px',
                display: 'inline-flex',
                alignItems: 'center',
              }}
            >
              <X size={14} />
            </button>
          </div>
        );
      })}
    </div>
  );
};
