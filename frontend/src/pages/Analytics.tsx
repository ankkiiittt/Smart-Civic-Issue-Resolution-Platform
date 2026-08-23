import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import type { Analytics as AnalyticsData, User } from '../services/api';
import { BarChart3, AlertTriangle, CheckCircle, Clock, TrendingUp } from 'lucide-react';

interface AnalyticsProps {
  user: User | null;
}

export const Analytics: React.FC<AnalyticsProps> = ({ user }) => {
  const [data, setData] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchAnalytics = async () => {
      setLoading(true);
      setError(null);
      try {
        const stats = await api.getAnalytics();
        setData(stats);
      } catch (err: any) {
        setError(err.message || 'Failed to fetch system analytics data.');
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
  }, []);

  if (!user || (user.role !== 'OFFICER' && user.role !== 'ADMIN')) {
    return (
      <div className="container" style={{ padding: '40px 0' }}>
        <div className="glass-panel" style={{ padding: '40px', color: 'var(--accent-danger)' }}>
          <AlertTriangle size={48} style={{ margin: '0 auto 16px auto' }} />
          <h2>Access Denied</h2>
          <p style={{ marginTop: '8px', color: 'var(--text-secondary)' }}>
            This dashboard is restricted to municipal officers and administrators.
          </p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="container" style={{ padding: '40px 0' }}>
        <div className="glass-panel" style={{ padding: '80px', color: 'var(--text-secondary)' }}>
          Loading dashboard metrics...
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="container" style={{ padding: '40px 0' }}>
        <div className="glass-panel" style={{ padding: '40px', color: 'var(--accent-danger)' }}>
          {error || 'No analytics data available.'}
        </div>
      </div>
    );
  }

  // Calculate percentages
  const maxIssuesByCategory = Math.max(...Object.values(data.issuesByCategory), 1);
  const maxResolutionTime = Math.max(...Object.values(data.averageResolutionTimeByCategory), 1);

  return (
    <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Title */}
      <div className="glass-panel" style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '16px', textAlign: 'left' }}>
        <BarChart3 size={32} style={{ color: 'var(--accent-primary)' }} />
        <div>
          <h2 style={{ fontSize: '24px' }}>System Performance Metrics</h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Live SLA compliance ratios, category counts, and weekly incident curves.
          </p>
        </div>
      </div>

      {/* Grid KPIs */}
      <div className="grid-3">
        
        {/* KPI: Total */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '16px', textAlign: 'left' }}>
          <div style={{ padding: '16px', background: 'rgba(99, 102, 241, 0.1)', borderRadius: 'var(--radius-md)', color: 'var(--accent-primary)' }}>
            <TrendingUp size={24} />
          </div>
          <div>
            <span style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>Total Incidents</span>
            <h3 style={{ fontSize: '28px', marginTop: '4px' }}>{data.totalIssues}</h3>
          </div>
        </div>

        {/* KPI: Resolved */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '16px', textAlign: 'left' }}>
          <div style={{ padding: '16px', background: 'rgba(16, 185, 129, 0.1)', borderRadius: 'var(--radius-md)', color: 'var(--accent-success)' }}>
            <CheckCircle size={24} />
          </div>
          <div>
            <span style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>Resolved Tickets</span>
            <h3 style={{ fontSize: '28px', marginTop: '4px' }}>{data.resolvedIssues}</h3>
          </div>
        </div>

        {/* KPI: SLA Breach Rate */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '16px', textAlign: 'left', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
          <div style={{ padding: '16px', background: 'rgba(239, 68, 68, 0.1)', borderRadius: 'var(--radius-md)', color: 'var(--accent-danger)' }}>
            <Clock size={24} />
          </div>
          <div>
            <span style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>SLA Breach Rate</span>
            <h3 style={{ fontSize: '28px', marginTop: '4px', color: 'var(--accent-danger)' }}>{data.slaBreachRate.toFixed(1)}%</h3>
          </div>
        </div>

      </div>

      {/* Main Charts Row */}
      <div className="grid-2">
        
        {/* Left Column: Counts By Category */}
        <div className="glass-panel" style={{ padding: '24px', textAlign: 'left' }}>
          <h3 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>Incident Volume By Category</span>
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {Object.entries(data.issuesByCategory).map(([category, count]) => {
              const percentage = (count / maxIssuesByCategory) * 100;
              return (
                <div key={category} style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                    <span style={{ fontWeight: 550 }}>{category}</span>
                    <strong style={{ color: 'var(--accent-primary)' }}>{count} issues</strong>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${percentage}%`, height: '100%', background: 'linear-gradient(90deg, var(--accent-primary) 0%, var(--accent-secondary) 100%)', borderRadius: '4px' }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right Column: Avg Resolution Hours */}
        <div className="glass-panel" style={{ padding: '24px', textAlign: 'left' }}>
          <h3 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>Average Resolution Speed (Hours)</span>
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {Object.entries(data.averageResolutionTimeByCategory).map(([category, time]) => {
              const percentage = (time / maxResolutionTime) * 100;
              return (
                <div key={category} style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                    <span style={{ fontWeight: 550 }}>{category}</span>
                    <strong style={{ color: 'var(--accent-success)' }}>{time.toFixed(1)} hrs</strong>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${percentage}%`, height: '100%', background: 'linear-gradient(90deg, #10b981 0%, #0ea5e9 100%)', borderRadius: '4px' }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

      </div>

      {/* Weekly Trends List */}
      <div className="glass-panel" style={{ padding: '24px', textAlign: 'left' }}>
        <h3 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span>Weekly Ticket Submission Trends</span>
        </h3>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '12px', textAlign: 'left' }}>Week Starting</th>
                <th style={{ padding: '12px', textAlign: 'right' }}>Issues Reported</th>
              </tr>
            </thead>
            <tbody>
              {data.weeklyVolumeTrends.map((t, idx) => (
                <tr key={idx} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '12px', fontWeight: 550 }}>{t.weekStartDate}</td>
                  <td style={{ padding: '12px', textAlign: 'right', color: 'var(--accent-primary)', fontWeight: 700 }}>{t.count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
};
