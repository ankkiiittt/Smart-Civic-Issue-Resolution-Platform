import { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { api } from './services/api'
import type { User } from './services/api'
import { connectWebSocket } from './services/websocket'
import type { WsMessage } from './services/websocket'
import { Navbar } from './components/Navbar'
import { ToastList } from './components/Toast'
import type { ToastItem } from './components/Toast'
import { Auth } from './pages/Auth'
import { Dashboard } from './pages/Dashboard'
import { OfficerPortal } from './pages/OfficerPortal'
import { Analytics } from './pages/Analytics'

function App() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [notifications, setNotifications] = useState<string[]>([])
  const [toasts, setToasts] = useState<ToastItem[]>([])

  // 1. Restore authentication session on load
  useEffect(() => {
    const token = localStorage.getItem('token')
    if (token) {
      api.getMe()
        .then((userData) => {
          setUser(userData)
        })
        .catch((err) => {
          console.warn('Session expired or invalid token:', err)
          localStorage.removeItem('token')
        })
        .finally(() => {
          setLoading(false)
        })
    } else {
      setLoading(false)
    }
  }, [])

  // 2. Establish WebSocket alert listener when authenticated
  useEffect(() => {
    if (!user) return

    const onUpdate = (msg: WsMessage) => {
      const displayMsg = `[Ticket #${msg.issueId}]: ${msg.message}`
      
      // Append notifications dropdown list
      setNotifications(prev => [displayMsg, ...prev])

      // Push custom floating toast alert
      const newToast: ToastItem = {
        id: Math.random().toString(36).substr(2, 9),
        message: msg.message,
        type: 'UPDATE'
      }
      setToasts(prev => [newToast, ...prev])
    }

    const onEscalation = (msg: WsMessage) => {
      const displayMsg = `🚨 [BREACH]: Issue #${msg.issueId} escalated to Critical SLA status!`

      setNotifications(prev => [displayMsg, ...prev])

      const newToast: ToastItem = {
        id: Math.random().toString(36).substr(2, 9),
        message: `Issue #${msg.issueId} has breached SLA deadline!`,
        type: 'ESCALATION'
      }
      setToasts(prev => [newToast, ...prev])
    }

    const disconnect = connectWebSocket(onUpdate, onEscalation)

    return () => {
      disconnect()
    }
  }, [user])

  const handleLogout = () => {
    localStorage.removeItem('token')
    setUser(null)
    setNotifications([])
  }

  const removeToast = (id: string) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: 'var(--bg-primary)', color: 'var(--text-secondary)' }}>
        Restoring session...
      </div>
    )
  }

  return (
    <BrowserRouter>
      <div className="main-layout">
        
        {/* Navigation Bar */}
        <Navbar 
          user={user} 
          onLogout={handleLogout} 
          notifications={notifications}
          clearNotifications={() => setNotifications([])}
        />

        {/* Content Body */}
        <main className="content-body">
          <Routes>
            <Route path="/" element={<Dashboard user={user} />} />
            
            <Route 
              path="/auth" 
              element={user ? <Navigate to="/" replace /> : <Auth onAuthSuccess={setUser} />} 
            />
            
            <Route 
              path="/portal" 
              element={user ? <OfficerPortal user={user} /> : <Navigate to="/auth" replace />} 
            />
            
            <Route 
              path="/analytics" 
              element={user ? <Analytics user={user} /> : <Navigate to="/auth" replace />} 
            />
            
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>

        {/* Floating Toast Notification overlay */}
        <ToastList toasts={toasts} onRemove={removeToast} />

      </div>
    </BrowserRouter>
  )
}

export default App
