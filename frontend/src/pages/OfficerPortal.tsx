import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import type { Issue, User, OfficerSummary } from '../services/api';
import { Shield, CheckCircle, AlertTriangle, Eye, UserPlus, UserCheck } from 'lucide-react';
import { SWAGGER_URL } from '../config';

interface OfficerPortalProps {
  user: User | null;
}

export const OfficerPortal: React.FC<OfficerPortalProps> = ({ user }) => {
  const [assignedIssues, setAssignedIssues] = useState<Issue[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Officers list for Admin assignment
  const [officers, setOfficers] = useState<OfficerSummary[]>([]);
  const [selectedOfficers, setSelectedOfficers] = useState<Record<number, number>>({});
  const [assigningId, setAssigningId] = useState<number | null>(null);
  const [assignmentSuccess, setAssignmentSuccess] = useState<string | null>(null);

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
    if (user?.role === 'ADMIN') {
      api.getOfficers()
        .then((data) => setOfficers(data))
        .catch((err) => console.error('Failed to load officers:', err));
    }
  }, [user]);

  const handleAssign = async (issueId: number) => {
    const officerId = selectedOfficers[issueId];
    if (!officerId) return;

    setAssigningId(issueId);
    try {
      await api.assignIssue(issueId, officerId);
      const officerObj = officers.find(o => o.id === officerId);
      setAssignmentSuccess(`Issue #${issueId} successfully assigned to ${officerObj ? officerObj.firstName + ' ' + officerObj.lastName : 'officer'}!`);
      setTimeout(() => setAssignmentSuccess(null), 4000);
      await fetchAssignedIssues();
    } catch (err: any) {
      alert(err.message || 'Failed to assign issue.');
    } finally {
      setAssigningId(null);
    }
  };

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
            {user.role === 'ADMIN' ? 'Viewing all system incidents (Administrator Mode — You can assign or reassign any incident below)' : `Viewing issues assigned to Officer ${user.firstName} ${user.lastName}`}
          </p>
        </div>
      </div>

      {assignmentSuccess && (
        <div style={{
          padding: '12px 18px',
          borderRadius: '8px',
          background: 'rgba(16, 185, 129, 0.15)',
          border: '1px solid var(--accent-success)',
          color: 'var(--accent-success)',
          fontSize: '14px',
          display: 'flex',
          alignItems: 'center',
          gap: '10px'
        }}>
          <CheckCircle size={18} />
          <span>{assignmentSuccess}</span>
        </div>
      )}

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

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', paddingTop: '8px', borderTop: '1px solid var(--border-color)' }}>
                  <UserCheck size={16} style={{ color: issue.assignedOfficerUsername ? 'var(--accent-success)' : 'var(--accent-warning)' }} />
                  <span style={{ color: 'var(--text-secondary)' }}>Assigned Officer:</span>
                  <strong style={{ color: issue.assignedOfficerUsername ? 'var(--accent-primary)' : 'var(--accent-warning)' }}>
                    {issue.assignedOfficerUsername ? `@${issue.assignedOfficerUsername}` : 'Unassigned (Awaiting Assignment)'}
                  </strong>
                </div>
              </div>

              {/* Right Column: Actions Forms */}
              <div style={{ flex: 1.2, minWidth: '280px', display: 'flex', flexDirection: 'column', gap: '14px', borderLeft: '1px solid var(--border-color)', paddingLeft: '24px' }}>
                
                {/* Admin Assignment Section */}
                {user.role === 'ADMIN' && (
                  <div style={{
                    padding: '14px',
                    borderRadius: '8px',
                    background: 'rgba(255, 255, 255, 0.03)',
                    border: '1px solid var(--border-color)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '10px'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: 650, color: 'var(--text-primary)' }}>
                      <UserPlus size={16} style={{ color: 'var(--accent-primary)' }} />
                      <span>{issue.assignedOfficerUsername ? 'Reassign Incident' : 'Assign Incident to Officer'}</span>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <select
                        className="form-input form-select"
                        style={{ width: '100%', padding: '7px 10px', fontSize: '12px' }}
                        value={selectedOfficers[issue.id] !== undefined ? selectedOfficers[issue.id] : (officers.find(o => o.username === issue.assignedOfficerUsername)?.id || '')}
                        onChange={(e) => setSelectedOfficers(prev => ({ ...prev, [issue.id]: Number(e.target.value) }))}
                      >
                        <option value="">-- Select an Officer --</option>
                        {officers.map((officer) => (
                          <option key={officer.id} value={officer.id}>
                            {officer.firstName} {officer.lastName} (@{officer.username}) — {officer.departmentName}
                          </option>
                        ))}
                      </select>

                      <button
                        className="btn btn-primary"
                        style={{ width: '100%', padding: '7px 12px', fontSize: '12px', gap: '6px' }}
                        onClick={() => handleAssign(issue.id)}
                        disabled={
                          assigningId === issue.id ||
                          !selectedOfficers[issue.id] ||
                          selectedOfficers[issue.id] === officers.find(o => o.username === issue.assignedOfficerUsername)?.id
                        }
                      >
                        <UserCheck size={14} />
                        <span>{assigningId === issue.id ? 'Assigning...' : (issue.assignedOfficerUsername ? 'Confirm Reassign' : 'Confirm Assignment')}</span>
                      </button>
                    </div>
                  </div>
                )}

                {activeIssueId !== issue.id ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    <button 
                      onClick={() => {
                        setActiveIssueId(issue.id);
                        setNewStatus(issue.status === 'REPORTED' ? 'ACKNOWLEDGED' : 'IN_PROGRESS');
                      }}
                      className="btn btn-secondary"
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
