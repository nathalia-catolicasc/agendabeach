import { useEffect, useState } from 'react'
import AdminPage from './AdminPage'
import BookingPage from './BookingPage'
import CancelBookingPage from './CancelBookingPage'
import MyReservationsPage from './MyReservationsPage'
import { AuthPage } from './components'
import { clearStoredSession, getStoredSession, storeSession, SESSION_CLEARED_EVENT } from './session'
import { logout } from './api'
import type { SessionUser } from './session'

function App() {
  const [user, setUser] = useState<SessionUser | null>(() => getStoredSession())
  const [currentPage, setCurrentPage] = useState('booking')

  useEffect(() => {
    const clear = () => {
      setUser(null)
      setCurrentPage('booking')
    }
    window.addEventListener(SESSION_CLEARED_EVENT, clear)
    return () => window.removeEventListener(SESSION_CLEARED_EVENT, clear)
  }, [])

  function handleLogin(loggedUser: SessionUser) {
    storeSession(loggedUser)
    setUser(loggedUser)
    setCurrentPage('booking')
  }

  function handleLogout() {
    void logout().catch(() => {
      /* O logout local continua disponível quando a API está indisponível. */
    })
    clearStoredSession()
    setUser(null)
    setCurrentPage('booking')
  }

  if (!user) {
    return <AuthPage onLogin={handleLogin} />
  }

  // A rota /admin também é renderizada para clientes, mas a própria AdminPage
  // mostra a tela de acesso restrito — e o backend recusa as ações com 403.
  if (currentPage === 'admin') {
    return <AdminPage user={user} onNavigate={setCurrentPage} onLogout={handleLogout} />
  }

  if (currentPage === 'cancel') {
    return <CancelBookingPage user={user} onNavigate={setCurrentPage} onLogout={handleLogout} />
  }

  if (currentPage === 'reservations') {
    return <MyReservationsPage user={user} onNavigate={setCurrentPage} onLogout={handleLogout} />
  }

  return <BookingPage user={user} onNavigate={setCurrentPage} onLogout={handleLogout} />
}

export default App
