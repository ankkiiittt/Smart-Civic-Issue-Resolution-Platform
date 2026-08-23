import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import type { User } from '../services/api';
import { Bell, LogOut, MapPin, BarChart3, ShieldAlert, User as UserIcon } from 'lucide-react';

interface NavbarProps {
  user: User | null;
  onLogout: () => void;
  notifications: string[];
  clearNotifications: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ user, onLogout, notifications, clearNotifications }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [showNotifications, setShowNotifications] = useState(false);

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="glass-panel" style={{ padding: '16px 24px', borderRadius: '0 0 var(--radius-md) var(--radius-md)', marginBottom: '24px' }}>
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: 0 }}>
        
        {/* Brand Logo */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-primary)', fontWeight: 700, fontSize: '20px', fontFamily: 'var(--font-heading)' }}>
          <MapPin className="text-indigo-500" style={{ color: 'var(--accent-primary)' }} size={24} />
          <span>SmartCivic</span>
        </Link>

        {/* Navigation Links (Role Dependent) */}
        {user && (
          <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
            <Link 
              to="/" 
              className={`btn ${isActive('/') ? 'btn-primary' : 'btn-secondary'}`}
              style={{ padding: '8px 16px', borderRadius: 'var(--radius-sm)' }}
            >
              Dashboard
            </Link>

            {/* Officer & Admin Portal */}
            {(user.role === 'OFFICER' || user.role === 'ADMIN') && (
              <Link 
                to="/portal" 
                className={`btn ${isActive('/portal') ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '8px 16px', borderRadius: 'var(--radius-sm)' }}
              >
                <ShieldAlert size={16} />
                <span>Officer Portal</span>
              </Link>
            )}

            {/* Admin Analytics */}
            {(user.role === 'ADMIN' || user.role === 'OFFICER') && (
              <Link 
                to="/analytics" 
                className={`btn ${isActive('/analytics') ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '8px 16px', borderRadius: 'var(--radius-sm)' }}
              >
                <BarChart3 size={16} />
                <span>Analytics</span>
              </Link>
            )}
          </div>
        )}

        {/* User Info / Notifications / Logout */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          {user ? (
            <>
              {/* Notification Center */}
              <div style={{ position: 'relative' }}>
                <button 
                  onClick={() => setShowNotifications(!showNotifications)}
                  className="btn btn-secondary" 
                  style={{ padding: '10px', borderRadius: 'var(--radius-full)', position: 'relative' }}
                >
                  <Bell size={18} />
                  {notifications.length > 0 && (
                    <span style={{
                      position: 'absolute',
                      top: '-4px',
                      right: '-4px',
                      background: 'var(--accent-danger)',
                      color: 'white',
                      fontSize: '10px',
                      borderRadius: '50%',
                      padding: '2px 6px',
                      fontWeight: 'bold'
                    }}>
                      {notifications.length}
                    </span>
                  )}
                </button>

                {/* Notifications Dropdown */}
                {showNotifications && (
                  <div className="glass-panel" style={{
                    position: 'absolute',
                    top: '50px',
                    right: 0,
                    width: '320px',
                    maxHeight: '400px',
                    overflowY: 'auto',
                    zIndex: 100,
                    padding: '16px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '12px'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <h4 style={{ fontSize: '15px' }}>Live Notifications</h4>
                      {notifications.length > 0 && (
                        <button 
                          onClick={clearNotifications}
                          style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', fontSize: '12px', cursor: 'pointer' }}
                        >
                          Clear All
                        </button>
                      )}
                    </div>
                    <hr style={{ borderColor: 'var(--border-color)', margin: '4px 0' }} />
                    {notifications.length === 0 ? (
                      <p style={{ fontSize: '13px', color: 'var(--text-muted)', textAlign: 'center', padding: '12px 0' }}>
                        No new notifications.
                      </p>
                    ) : (
                      notifications.map((notif, index) => (
                        <div key={index} style={{
                          padding: '10px',
                          background: 'rgba(255, 255, 255, 0.03)',
                          borderLeft: '3px solid var(--accent-secondary)',
                          borderRadius: '4px',
                          fontSize: '13px',
                          textAlign: 'left'
                        }}>
                          {notif}
                        </div>
                      ))
                    )}
                  </div>
                )}
              </div>

              {/* Profile Card */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255, 255, 255, 0.03)', padding: '6px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                <UserIcon size={16} style={{ color: 'var(--text-secondary)' }} />
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                  <span style={{ fontSize: '13px', fontWeight: 600 }}>{user.firstName} {user.lastName}</span>
                  <span style={{ fontSize: '11px', color: 'var(--accent-primary)', fontWeight: 700 }}>{user.role}</span>
                </div>
              </div>

              {/* Logout Button */}
              <button 
                onClick={onLogout} 
                className="btn btn-danger" 
                style={{ padding: '10px', borderRadius: 'var(--radius-sm)' }}
                title="Log Out"
              >
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <Link to="/auth" className="btn btn-primary">Sign In / Register</Link>
          )}
        </div>

      </div>
    </nav>
  );
};
