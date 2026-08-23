import React from 'react';
import type { Issue } from '../services/api';
import { ThumbsUp, Calendar, Clock, AlertTriangle, User, UserCheck } from 'lucide-react';

interface IssueCardProps {
  issue: Issue;
  onUpvoteToggle?: (id: number, hasUpvoted: boolean) => void;
  onSelect?: (id: number) => void;
  onDelete?: (id: number) => void;
  currentUsername?: string | null;
}

export const IssueCard: React.FC<IssueCardProps> = ({
  issue,
  onUpvoteToggle,
  onSelect,
  onDelete,
  currentUsername,
}) => {
  const getStatusLabel = (status: string) => {
    return status.replace('_', ' ');
  };

  const getPriorityBadgeClass = (priority: string) => {
    switch (priority) {
      case 'LOW': return 'badge-priority-low';
      case 'MEDIUM': return 'badge-priority-medium';
      case 'HIGH': return 'badge-priority-high';
      case 'CRITICAL': return 'badge-priority-critical';
      default: return '';
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'REPORTED': return 'badge-reported';
      case 'ACKNOWLEDGED': return 'badge-acknowledged';
      case 'IN_PROGRESS': return 'badge-in_progress';
      case 'RESOLVED': return 'badge-resolved';
      default: return '';
    }
  };

  // Check if SLA deadline is overdue
  const isOverdue = new Date(issue.slaDeadline) < new Date() && issue.status !== 'RESOLVED';

  // Format Dates
  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString(undefined, {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="glass-card" style={{ padding: '20px', textAlign: 'left', display: 'flex', flexDirection: 'column', gap: '14px', position: 'relative' }}>
      
      {/* Badges row */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
        <div style={{ display: 'flex', gap: '6px' }}>
          <span className={`badge ${getStatusBadgeClass(issue.status)}`}>
            {getStatusLabel(issue.status)}
          </span>
          <span className={`badge ${getPriorityBadgeClass(issue.priority)}`}>
            {issue.priority}
          </span>
        </div>
        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
          #{issue.id}
        </span>
      </div>

      {/* Main Title & Description */}
      <div style={{ cursor: onSelect ? 'pointer' : 'default' }} onClick={() => onSelect && onSelect(issue.id)}>
        <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '6px' }}>
          {issue.title}
        </h3>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {issue.description}
        </p>
      </div>

      {/* Photos Thumbnail strip */}
      {issue.photos && issue.photos.length > 0 && (
        <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
          {issue.photos.map((p) => (
            <img 
              key={p.id}
              src={`http://localhost:8080${p.photoUrl}`}
              alt="Issue evidence"
              style={{
                width: '64px',
                height: '64px',
                objectFit: 'cover',
                borderRadius: '6px',
                border: '1px solid var(--border-color)',
                cursor: 'pointer'
              }}
              onClick={() => window.open(`http://localhost:8080${p.photoUrl}`, '_blank')}
            />
          ))}
        </div>
      )}

      <hr style={{ borderColor: 'var(--border-color)', margin: '4px 0 0 0' }} />

      {/* Date Timelines & SLA indicators */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '13px', color: 'var(--text-secondary)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <Calendar size={14} style={{ color: 'var(--text-muted)' }} />
          <span>Reported: {formatDate(issue.reportedAt)}</span>
        </div>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: isOverdue ? 'var(--accent-danger)' : 'var(--text-secondary)' }}>
          <Clock size={14} style={{ color: isOverdue ? 'var(--accent-danger)' : 'var(--text-muted)' }} />
          <span>SLA Deadline: {formatDate(issue.slaDeadline)}</span>
          {isOverdue && (
            <span style={{ fontSize: '11px', fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: '2px', background: 'rgba(239,68,68,0.1)', padding: '2px 6px', borderRadius: '4px' }}>
              <AlertTriangle size={10} /> Overdue
            </span>
          )}
        </div>
      </div>

      {/* Assignment Status */}
      <div style={{ display: 'flex', gap: '16px', fontSize: '12px', color: 'var(--text-secondary)', background: 'rgba(255,255,255,0.02)', padding: '8px 12px', borderRadius: 'var(--radius-sm)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <User size={13} style={{ color: 'var(--text-muted)' }} />
          <span>By: {issue.reporterUsername}</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <UserCheck size={13} style={{ color: 'var(--text-muted)' }} />
          <span>Officer: {issue.assignedOfficerUsername || 'Unassigned'}</span>
        </div>
      </div>

      {/* Footer Upvote controls & Details view CTA */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
        
        <div style={{ display: 'flex', gap: '8px' }}>
          {/* Upvote Button */}
          <button
            onClick={() => onUpvoteToggle && onUpvoteToggle(issue.id, issue.hasUpvoted)}
            className={`btn ${issue.hasUpvoted ? 'btn-primary' : 'btn-secondary'}`}
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-sm)',
              fontSize: '13px',
              gap: '6px',
              cursor: onUpvoteToggle ? 'pointer' : 'default'
            }}
            disabled={!onUpvoteToggle}
          >
            <ThumbsUp size={14} />
            <span>{issue.upvotesCount} {issue.hasUpvoted ? 'Upvoted' : 'Upvote'}</span>
          </button>

          {/* Delete Button */}
          {currentUsername === issue.reporterUsername && onDelete && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                if (window.confirm('Are you sure you want to delete this report?')) {
                  onDelete(issue.id);
                }
              }}
              className="btn btn-secondary"
              style={{
                padding: '6px 12px',
                borderRadius: 'var(--radius-sm)',
                fontSize: '13px',
                color: 'var(--accent-danger)',
                border: '1px solid rgba(239, 68, 68, 0.2)',
                cursor: 'pointer'
              }}
            >
              Delete
            </button>
          )}
        </div>

        {/* View Details Link */}
        {onSelect && (
          <button 
            onClick={() => onSelect(issue.id)}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--accent-primary)',
              fontWeight: 600,
              fontSize: '13px',
              cursor: 'pointer',
              padding: '6px 12px'
            }}
          >
            View Details &rarr;
          </button>
        )}
      </div>

    </div>
  );
};
