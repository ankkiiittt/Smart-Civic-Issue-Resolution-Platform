import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import type { Issue, Category, User } from '../services/api';
import { IssueMap } from '../components/IssueMap';
import { IssueCard } from '../components/IssueCard';
import { MapPin, Plus, Search, Filter, Compass, Image as ImageIcon, X, AlertCircle, ThumbsUp } from 'lucide-react';
import { getPhotoUrl } from '../config';

interface DashboardProps {
  user: User | null;
}

// Default Map Center: Bengaluru (matches seeded officer polygons)
const MAP_CENTER: [number, number] = [12.9716, 77.5946];

export const Dashboard: React.FC<DashboardProps> = ({ user }) => {
  const [issues, setIssues] = useState<Issue[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [categories, setCategories] = useState<Category[]>([]);
  
  // Search & Filters state
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | ''>('');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(6);

  // Proximity filter state
  const [nearbyMode, setNearbyMode] = useState(false);
  const [radius, setRadius] = useState(2000); // meters

  // Selected Issue Detail modal
  const [selectedIssueId, setSelectedIssueId] = useState<number | null>(null);
  const [selectedIssue, setSelectedIssue] = useState<Issue | null>(null);

  // Report Issue Modal
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportTitle, setReportTitle] = useState('');
  const [reportDescription, setReportDescription] = useState('');
  const [reportCategory, setReportCategory] = useState<number>(1);
  const [reportPriority, setReportPriority] = useState<'LOW' | 'MEDIUM' | 'HIGH'>('MEDIUM');
  const [reportLat, setReportLat] = useState<number | null>(null);
  const [reportLng, setReportLng] = useState<number | null>(null);
  const [reportPhotos, setReportPhotos] = useState<File[]>([]);
  const [reportError, setReportError] = useState<string | null>(null);
  const [reportSuccess, setReportSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Categories loading
  useEffect(() => {
    // Static mock list based on seeded values
    setCategories([
      { id: 1, name: 'Pothole Repair', description: 'Road potholes', targetSlaHours: 48 },
      { id: 2, name: 'Streetlight Outage', description: 'Broken lights', targetSlaHours: 24 },
      { id: 3, name: 'Garbage Overflow', description: 'Piles of waste', targetSlaHours: 12 },
      { id: 4, name: 'Water Pipe Burst', description: 'Water leakage', targetSlaHours: 8 },
      { id: 5, name: 'Public Park Damage', description: 'Broken benches', targetSlaHours: 72 },
    ]);
  }, []);

  // Primary Issues fetcher
  const fetchIssues = async () => {
    try {
      if (nearbyMode) {
        // Fetch by distance
        const data = await api.getNearbyIssues(MAP_CENTER[1], MAP_CENTER[0], radius);
        setIssues(data);
        setTotalElements(data.length);
      } else {
        // Standard paginated fetch with filters
        const data = await api.getIssues({
          page,
          size: pageSize,
          status: selectedStatus || undefined,
          categoryId: selectedCategory || undefined,
          query: searchQuery || undefined,
        });
        setIssues(data.content);
        setTotalElements(data.totalElements);
      }
    } catch (err) {
      console.error('Error loading issues', err);
    }
  };

  useEffect(() => {
    fetchIssues();
  }, [page, selectedStatus, selectedCategory, nearbyMode, radius]);

  // Handle detailed issue fetch
  useEffect(() => {
    if (selectedIssueId !== null) {
      api.getIssue(selectedIssueId)
        .then(setSelectedIssue)
        .catch(err => console.error(err));
    } else {
      setSelectedIssue(null);
    }
  }, [selectedIssueId]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchIssues();
  };

  const handleUpvoteToggle = async (id: number, hasUpvoted: boolean) => {
    if (!user) {
      alert('Please log in to upvote issues!');
      return;
    }
    try {
      if (hasUpvoted) {
        await api.removeUpvote(id);
      } else {
        await api.upvote(id);
      }
      
      // Update local state smoothly
      setIssues(prev => prev.map(issue => {
        if (issue.id === id) {
          return {
            ...issue,
            hasUpvoted: !hasUpvoted,
            upvotesCount: hasUpvoted ? issue.upvotesCount - 1 : issue.upvotesCount + 1
          };
        }
        return issue;
      }));

      // Update selected issue modal if open
      if (selectedIssue && selectedIssue.id === id) {
        setSelectedIssue(prev => prev ? {
          ...prev,
          hasUpvoted: !hasUpvoted,
          upvotesCount: hasUpvoted ? prev.upvotesCount - 1 : prev.upvotesCount + 1
        } : null);
      }
    } catch (err) {
      console.error('Upvote toggle failed', err);
    }
  };

  const handleDeleteIssue = async (id: number) => {
    try {
      await api.deleteIssue(id);
      if (selectedIssueId === id) {
        setSelectedIssueId(null);
        setSelectedIssue(null);
      }
      fetchIssues();
    } catch (err: any) {
      alert(err.message || 'Failed to delete issue.');
    }
  };

  const handleMapClick = (lat: number, lng: number) => {
    if (showReportModal) {
      setReportLat(lat);
      setReportLng(lng);
    }
  };

  const openReportModal = () => {
    if (!reportLat || !reportLng) {
      setReportLat(MAP_CENTER[0]);
      setReportLng(MAP_CENTER[1]);
    }
    setShowReportModal(true);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setReportPhotos(Array.from(e.target.files));
    }
  };

  const handleReportSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setReportError(null);
    setReportSuccess(false);

    if (!reportLat || !reportLng) {
      setReportError('Please click on the map to place a pin at the issue location.');
      return;
    }

    setSubmitting(true);
    try {
      const issueRequest = {
        title: reportTitle,
        description: reportDescription,
        categoryId: reportCategory,
        priority: reportPriority,
        latitude: reportLat,
        longitude: reportLng,
      };

      await api.reportIssue(issueRequest, reportPhotos);
      setReportSuccess(true);
      
      // Reset form
      setReportTitle('');
      setReportDescription('');
      setReportLat(null);
      setReportLng(null);
      setReportPhotos([]);
      
      // Reload issues list
      fetchIssues();
      
      // Close after delay
      setTimeout(() => {
        setShowReportModal(false);
        setReportSuccess(false);
      }, 1500);
    } catch (err: any) {
      setReportError(err.message || 'Failed to submit issue. Ensure fields are valid.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Top Action & Search Bar */}
      <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'center', gap: '16px' }}>
        
        {/* Search Input */}
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '10px', flex: 1, minWidth: '280px' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <Search size={16} style={{ position: 'absolute', left: '16px', top: '15px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-input"
              style={{ paddingLeft: '44px' }}
              placeholder="Search issues by title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <button type="submit" className="btn btn-secondary">Search</button>
        </form>

        {/* Action Button */}
        {user ? (
          <button 
            onClick={openReportModal} 
            className="btn btn-primary"
            style={{ padding: '12px 24px', fontSize: '15px' }}
          >
            <Plus size={18} />
            <span>Report Civic Issue</span>
          </button>
        ) : (
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            Sign in to start reporting issues.
          </p>
        )}
      </div>

      {/* Main Grid View */}
      <div className="grid-map-list">
        
        {/* Left Side: Map Container */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', height: '100%' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ fontSize: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Compass size={16} style={{ color: 'var(--accent-primary)' }} />
              <span>Interactive Incident Map</span>
            </h3>
            <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
              {showReportModal ? '🔴 Click map to select coordinates' : '🟢 Click pins to inspect details'}
            </span>
          </div>

          <div style={{ flex: 1, minHeight: '480px' }}>
            <IssueMap
              issues={issues}
              center={MAP_CENTER}
              onMapClick={handleMapClick}
              selectedLocation={reportLat && reportLng ? [reportLat, reportLng] : null}
              onSelectIssue={setSelectedIssueId}
            />
          </div>
        </div>

        {/* Right Side: List & Filters */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {/* List Settings Panel */}
          <div className="glass-panel" style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
            
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', fontWeight: 600 }}>
              <Filter size={16} style={{ color: 'var(--accent-primary)' }} />
              <span>Filter Parameters</span>
            </div>

            {/* Standard Filters */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <select 
                className="form-input form-select" 
                style={{ padding: '8px 12px' }}
                value={selectedStatus}
                onChange={(e) => { setSelectedStatus(e.target.value); setNearbyMode(false); setPage(0); }}
              >
                <option value="">All Statuses</option>
                <option value="REPORTED">Reported</option>
                <option value="ACKNOWLEDGED">Acknowledged</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="RESOLVED">Resolved</option>
              </select>

              <select 
                className="form-input form-select"
                style={{ padding: '8px 12px' }}
                value={selectedCategory}
                onChange={(e) => { setSelectedCategory(e.target.value ? Number(e.target.value) : ''); setNearbyMode(false); setPage(0); }}
              >
                <option value="">All Categories</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>

            {/* Proximity Radius Filter */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={nearbyMode}
                  onChange={(e) => { setNearbyMode(e.target.checked); setPage(0); }}
                />
                <span>Enable Proximity Search</span>
              </label>

              {nearbyMode && (
                <select
                  className="form-input form-select"
                  style={{ width: '130px', padding: '4px 8px', fontSize: '12px' }}
                  value={radius}
                  onChange={(e) => setRadius(Number(e.target.value))}
                >
                  <option value={500}>500 Meters</option>
                  <option value={1000}>1 Kilometer</option>
                  <option value={2000}>2 Kilometers</option>
                  <option value={5000}>5 Kilometers</option>
                </select>
              )}
            </div>

          </div>

          {/* Incident List */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', overflowY: 'auto', maxHeight: '520px', paddingRight: '4px' }}>
            {issues.length === 0 ? (
              <div className="glass-panel" style={{ padding: '40px 20px', color: 'var(--text-secondary)' }}>
                No reported incidents match your search coordinates or filter parameters.
              </div>
            ) : (
              issues.map((issue) => (
                <IssueCard
                  key={issue.id}
                  issue={issue}
                  onUpvoteToggle={handleUpvoteToggle}
                  onSelect={setSelectedIssueId}
                  onDelete={handleDeleteIssue}
                  currentUsername={user?.username}
                />
              ))
            )}
          </div>

          {/* Pagination Controls */}
          {!nearbyMode && totalElements > pageSize && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', marginTop: '8px' }}>
              <button 
                className="btn btn-secondary" 
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                &larr; Prev
              </button>
              <span style={{ display: 'inline-flex', alignItems: 'center', fontSize: '14px', color: 'var(--text-secondary)' }}>
                Page {page + 1} of {Math.ceil(totalElements / pageSize)}
              </span>
              <button 
                className="btn btn-secondary" 
                onClick={() => setPage(p => p + 1)}
                disabled={(page + 1) * pageSize >= totalElements}
              >
                Next &rarr;
              </button>
            </div>
          )}

        </div>

      </div>

      {/* REPORT ISSUE MODAL */}
      {showReportModal && (
        <div className="modal-overlay">
          <div className="glass-panel modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '20px' }}>Report a New Incident</h3>
              <button onClick={() => setShowReportModal(false)} className="btn btn-secondary" style={{ padding: '6px', borderRadius: '50%' }}>
                <X size={16} />
              </button>
            </div>

            {reportError && (
              <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#f87171', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '12px', borderRadius: '4px', fontSize: '13px', marginBottom: '16px', display: 'flex', gap: '8px', alignItems: 'center' }}>
                <AlertCircle size={16} />
                <span>{reportError}</span>
              </div>
            )}

            {reportSuccess && (
              <div style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#34d399', border: '1px solid rgba(16, 185, 129, 0.2)', padding: '12px', borderRadius: '4px', fontSize: '13px', marginBottom: '16px', textAlign: 'center' }}>
                Incident reported successfully!
              </div>
            )}

            <form onSubmit={handleReportSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              
              <div className="form-group">
                <label className="form-label">Incident Title</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Broken Water Pipe near Central Park"
                  value={reportTitle}
                  onChange={(e) => setReportTitle(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Detailed Description</label>
                <textarea
                  className="form-input"
                  style={{ minHeight: '80px', resize: 'vertical' }}
                  placeholder="Provide precise details of the damage, leak size, or hazard levels..."
                  value={reportDescription}
                  onChange={(e) => setReportDescription(e.target.value)}
                  required
                />
              </div>

              <div style={{ display: 'flex', gap: '16px' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label">Category</label>
                  <select
                    className="form-input form-select"
                    value={reportCategory}
                    onChange={(e) => setReportCategory(Number(e.target.value))}
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label">Urgency Priority</label>
                  <select
                    className="form-input form-select"
                    value={reportPriority}
                    onChange={(e) => setReportPriority(e.target.value as any)}
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Location Pin</label>
                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                  <MapPin size={18} style={{ color: 'var(--accent-danger)' }} />
                  {reportLat && reportLng ? (
                    <span style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
                      Latitude: {reportLat.toFixed(6)}, Longitude: {reportLng.toFixed(6)}
                    </span>
                  ) : (
                    <span style={{ fontSize: '13px', color: 'var(--accent-warning)', fontWeight: 600 }}>
                      * Click on the map behind this window to place your pin!
                    </span>
                  )}
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Evidence Photos</label>
                <div style={{ border: '1px dashed var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '20px', textAlign: 'center', cursor: 'pointer', position: 'relative' }}>
                  <ImageIcon size={32} style={{ color: 'var(--text-muted)', marginBottom: '8px' }} />
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>Click to upload incident photos (JPEG/PNG)</p>
                  <input
                    type="file"
                    multiple
                    accept="image/*"
                    onChange={handleFileChange}
                    style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, opacity: 0, cursor: 'pointer' }}
                  />
                </div>
                {reportPhotos.length > 0 && (
                  <div style={{ fontSize: '12px', color: 'var(--accent-success)', display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                    {reportPhotos.map((f, i) => (
                      <span key={i} style={{ background: 'rgba(16, 185, 129, 0.1)', padding: '2px 8px', borderRadius: '4px' }}>
                        {f.name}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: '100%', padding: '12px', fontSize: '15px' }}
                disabled={submitting}
              >
                {submitting ? 'Reporting issue...' : 'Submit Incident Report'}
              </button>

            </form>
          </div>
        </div>
      )}

      {/* DETAILED ISSUE POPUP MODAL */}
      {selectedIssue && (
        <div className="modal-overlay">
          <div className="glass-panel modal-content" style={{ maxWidth: '680px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '22px' }}>Incident Detail #{selectedIssue.id}</h3>
              <button onClick={() => setSelectedIssueId(null)} className="btn btn-secondary" style={{ padding: '6px', borderRadius: '50%' }}>
                <X size={16} />
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', textAlign: 'left' }}>
              
              {/* Header Titles */}
              <div>
                <h4 style={{ fontSize: '18px', color: 'var(--text-primary)', marginBottom: '8px' }}>
                  {selectedIssue.title}
                </h4>
                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                  <span className="badge badge-reported">{selectedIssue.category.name}</span>
                  <span className="badge badge-acknowledged">{selectedIssue.status}</span>
                  {selectedIssue.isEscalated && (
                    <span className="badge badge-priority-critical">Escalated</span>
                  )}
                </div>
              </div>

              {/* Description */}
              <div>
                <h5 style={{ fontSize: '14px', color: 'var(--text-muted)', marginBottom: '4px' }}>Description</h5>
                <p style={{ fontSize: '14px', color: 'var(--text-secondary)', background: 'rgba(255,255,255,0.02)', padding: '12px', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
                  {selectedIssue.description}
                </p>
              </div>

              {/* Photos Gallery */}
              {selectedIssue.photos && selectedIssue.photos.length > 0 && (
                <div>
                  <h5 style={{ fontSize: '14px', color: 'var(--text-muted)', marginBottom: '6px' }}>Incident Photos</h5>
                  <div style={{ display: 'flex', gap: '12px', overflowX: 'auto' }}>
                    {selectedIssue.photos.map((p) => (
                      <div key={p.id} style={{ position: 'relative' }}>
                        <img 
                          src={getPhotoUrl(p.photoUrl)}
                          alt="Incident view"
                          style={{ width: '120px', height: '120px', objectFit: 'cover', borderRadius: '8px', border: '1px solid var(--border-color)' }}
                        />
                        <span style={{
                          position: 'absolute',
                          bottom: '6px',
                          left: '6px',
                          fontSize: '9px',
                          background: p.isBefore ? 'var(--accent-primary)' : 'var(--accent-success)',
                          color: 'white',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          fontWeight: 700
                        }}>
                          {p.isBefore ? 'Before' : 'After'}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Detail fields */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', background: 'rgba(255,255,255,0.01)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)', fontSize: '13px' }}>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Reporter:</span><br/>
                  <strong>{selectedIssue.reporterUsername}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Assigned Officer:</span><br/>
                  <strong>{selectedIssue.assignedOfficerUsername || 'Unassigned'}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Latitude / Longitude:</span><br/>
                  <strong>{selectedIssue.latitude.toFixed(5)}, {selectedIssue.longitude.toFixed(5)}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>SLA target:</span><br/>
                  <strong>{selectedIssue.category.targetSlaHours} Hours</strong>
                </div>
              </div>

              {/* Audit history logs timeline */}
              <div>
                <h5 style={{ fontSize: '14px', color: 'var(--text-muted)', marginBottom: '8px' }}>Timeline Audit Logs</h5>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {selectedIssue.histories && selectedIssue.histories.map((h, i) => (
                    <div key={h.id} style={{ display: 'flex', gap: '12px', fontSize: '13px' }}>
                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--accent-primary)' }} />
                        {i < selectedIssue.histories.length - 1 && (
                          <div style={{ width: '2px', flex: 1, background: 'var(--border-color)', margin: '4px 0' }} />
                        )}
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <strong style={{ color: 'var(--text-primary)' }}>{h.status}</strong>
                          <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{new Date(h.changedAt).toLocaleString()}</span>
                        </div>
                        <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                          By {h.changedByUsername} {h.comment ? `— "${h.comment}"` : ''}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Upvote Toggle & Delete Button */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '10px' }}>
                <button
                  onClick={() => handleUpvoteToggle(selectedIssue.id, selectedIssue.hasUpvoted)}
                  className={`btn ${selectedIssue.hasUpvoted ? 'btn-primary' : 'btn-secondary'}`}
                  style={{ padding: '8px 16px', borderRadius: 'var(--radius-sm)' }}
                >
                  <ThumbsUp size={14} style={{ marginRight: '6px' }} />
                  <span>{selectedIssue.upvotesCount} Upvotes</span>
                </button>

                {user?.username === selectedIssue.reporterUsername && (
                  <button
                    onClick={() => {
                      if (window.confirm('Are you sure you want to delete this report?')) {
                        handleDeleteIssue(selectedIssue.id);
                      }
                    }}
                    className="btn btn-secondary"
                    style={{
                      padding: '8px 16px',
                      borderRadius: 'var(--radius-sm)',
                      color: 'var(--accent-danger)',
                      border: '1px solid rgba(239, 68, 68, 0.2)',
                      cursor: 'pointer'
                    }}
                  >
                    Delete Report
                  </button>
                )}
              </div>

            </div>
          </div>
        </div>
      )}

    </div>
  );
};
