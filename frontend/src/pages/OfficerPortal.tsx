import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import type { Issue, User } from '../services/api';
import { Shield, CheckCircle, AlertTriangle, Eye } from 'lucide-react';
import { SWAGGER_URL } from '../config';

interface OfficerPortalProps {
  user: User | null;
}

export const OfficerPortal: React.FC<OfficerPortalProps> = ({ user }) => {
  const [assignedIssues, setAssignedIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Status transition state
  const [activeIssueId, setActiveIssueId] = useState<number | null>(null);
  const [newStatus, setNewStatus] = useState<string>('ACKNOWLEDGED');
  const [comment, setComment] = useState('');
  const [resolutionPhoto, setResolutionPhoto] = useState<File | null>(null);
  const [updating, setUpdating] = useState(false);

  const fetchAssignedIssues = async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    try {
      // Fetch all issues (we filter locally by assigned officer for mock safety, or let backend roles match)
      const data = await api.getIssues({ size: 100 });
      
      const filtered = data.content.filter(
        (issue) => 
          issue.assignedOfficerUsername === user.username || 
          user.role === 'ADMIN'
      );
      
      setAssignedIssues(filtered);
    } catch (err: any) {
      setError(err.message || 'Failed to load assigned issues.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAssignedIssues();
  }, [user]);

  const handleStatusUpdateSubmit = async (e: React.FormEvent, issueId: number) => {
    e.preventDefault();
    setUpdating(true);

    try {
      const payload = {
        status: newStatus,
        comment: comment || undefined
      };

      await api.updateIssueStatus(issueId, payload, resolutionPhoto || undefined);
      
      // Reset state
      setActiveIssueId(null);
      setComment('');
      setResolutionPhoto(null);
      
      // Reload list
      fetchAssignedIssues();
    } catch (err: any) {
      alert(err.message || 'Status transition failed.');
    } finally {
      setUpdating(false);
    }
  };

  if (!user || (user.role !== 'OFFICER' && user.role !== 'ADMIN')) {
    return (
      <div className="container" style={{ padding: '40px 0' }}>
        <div className="glass-panel" style={{ padding: '40px', color: 'var(--accent-danger)' }}>
          <AlertTriangle size={48} style={{ margin: '0 auto 16px auto' }} />
          <h2>Access Denied</h2>
          <p style={{ marginTop: '8px', color: 'var(--text-secondary)' }}>
            This page is restricted to municipal officers and platform administrators.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Title Banner */}
      <div className="glass-panel" style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '16px', textAlign: 'left' }}>
        <Shield size={32} style={{ color: 'var(--accent-primary)' }} />
        <div>
          <h2 style={{ fontSize: '24px' }}>Municipal Officer Workspace</h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            {user.role === 'ADMIN' ? 'Viewing all system incidents (Administrator Mode)' : `Viewing issues assigned to Officer ${user.firstName} ${user.lastName}`}
          </p>
        </div>
      </div>

      {loading ? (
        <div className="glass-panel" style={{ padding: '80px', color: 'var(--text-secondary)' }}>
          Loading assigned incidents...
        </div>
      ) : error ? (
        <div className="glass-panel" style={{ padding: '40px', color: 'var(--accent-danger)' }}>
          {error}
        </div>
      ) : assignedIssues.length === 0 ? (
        <div className="glass-panel" style={{ padding: '80px', color: 'var(--text-secondary)' }}>
          No incidents are currently assigned to your duty profile.
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {assignedIssues.map((issue) => (
            <div key={issue.id} className="glass-panel" style={{ padding: '24px', textAlign: 'left', display: 'flex', flexWrap: 'wrap', gap: '24px', justifyContent: 'space-between', borderLeft: issue.isEscalated ? '4px solid var(--accent-danger)' : '1px solid var(--border-color)' }}>
              
              {/* Left Column: Title & Params */}
              <div style={{ flex: 2, minWidth: '300px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <span className={`badge ${
                    issue.status === 'REPORTED' ? 'badge-reported' :
                    issue.status === 'ACKNOWLEDGED' ? 'badge-acknowledged' :
                    issue.status === 'IN_PROGRESS' ? 'badge-in_progress' : 'badge-resolved'
                  }`}>
                    {issue.status}
                  </span>
                  <span style={{ fontSize: '12px', color: 'var(--text-secondary)', fontWeight: 600 }}>
                    Category: {issue.category.name}
                  </span>
                  {issue.isEscalated && (
                    <span className="badge badge-priority-critical" style={{ fontSize: '10px' }}>
                      Escalated
                    </span>
                  )}
                </div>

                <div>
                  <h3 style={{ fontSize: '18px', fontWeight: 650, color: 'var(--text-primary)' }}>
                    {issue.title}
                  </h3>
                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '6px' }}>
                    {issue.description}
                  </p>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '12px', color: 'var(--text-muted)' }}>
                  <span>Reported by: <strong>{issue.reporterUsername}</strong></span>
                  <span>SLA deadline: <strong>{new Date(issue.slaDeadline).toLocaleString()}</strong></span>
                  <span>Latitude: <strong>{issue.latitude.toFixed(5)}</strong></span>
                  <span>Longitude: <strong>{issue.longitude.toFixed(5)}</strong></span>
                </div>
              </div>

              {/* Right Column: Actions Forms */}
              <div style={{ flex: 1.2, minWidth: '280px', display: 'flex', flexDirection: 'column', gap: '12px', borderLeft: '1px solid var(--border-color)', paddingLeft: '24px' }}>
                
                {activeIssueId !== issue.id ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', height: '100%', justifyContent: 'center' }}>
                    <button 
                      onClick={() => {
                        setActiveIssueId(issue.id);
                        setNewStatus(issue.status === 'REPORTED' ? 'ACKNOWLEDGED' : 'IN_PROGRESS');
                      }}
                      className="btn btn-primary"
                      style={{ width: '100%' }}
                      disabled={issue.status === 'RESOLVED'}
                    >
                      {issue.status === 'RESOLVED' ? (
                        <>
                          <CheckCircle size={14} />
                          <span>Incident Resolved</span>
                        </>
                      ) : (
                        'Update Incident Status'
                      )}
                    </button>
                    
                    <button
                      onClick={() => window.open(SWAGGER_URL, '_blank')}
                      className="btn btn-secondary"
                      style={{ width: '100%', gap: '6px' }}
                    >
                      <Eye size={14} />
                      <span>Inspect Audit Logs</span>
                    </button>
                  </div>
                ) : (
                  <form onSubmit={(e) => handleStatusUpdateSubmit(e, issue.id)} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label" style={{ fontSize: '12px' }}>New Status</label>
                      <select
                        className="form-input form-select"
                        style={{ padding: '6px 12px', fontSize: '13px' }}
                        value={newStatus}
                        onChange={(e) => setNewStatus(e.target.value)}
                      >
                        <option value="ACKNOWLEDGED">Acknowledge</option>
                        <option value="IN_PROGRESS">Mark In Progress</option>
                        <option value="RESOLVED">Mark Resolved</option>
                      </select>
                    </div>

                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label" style={{ fontSize: '12px' }}>Status Comment</label>
                      <input
                        type="text"
                        className="form-input"
                        style={{ padding: '6px 12px', fontSize: '13px' }}
                        placeholder="Write work progress details..."
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        required
                      />
                    </div>

                    {newStatus === 'RESOLVED' && (
                      <div className="form-group" style={{ marginBottom: 0 }}>
                        <label className="form-label" style={{ fontSize: '12px' }}>Resolution Photo</label>
                        <input
                          type="file"
                          accept="image/*"
                          style={{ fontSize: '11px', color: 'var(--text-secondary)' }}
                          onChange={(e) => setResolutionPhoto(e.target.files ? e.target.files[0] : null)}
                          required
                        />
                      </div>
                    )}

                    <div style={{ display: 'flex', gap: '8px', marginTop: '4px' }}>
                      <button
                        type="submit"
                        className="btn btn-primary"
                        style={{ flex: 1, padding: '8px' }}
                        disabled={updating}
                      >
                        {updating ? 'Saving...' : 'Save'}
                      </button>
                      <button
                        type="button"
                        className="btn btn-secondary"
                        style={{ flex: 1, padding: '8px' }}
                        onClick={() => { setActiveIssueId(null); setComment(''); setResolutionPhoto(null); }}
                      >
                        Cancel
                      </button>
                    </div>

                  </form>
                )}

              </div>

            </div>
          ))}
        </div>
      )}

    </div>
  );
};
