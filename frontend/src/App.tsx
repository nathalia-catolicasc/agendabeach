import { useState } from 'react'
import BookingPage from './BookingPage'
import MyReservationsPage from './MyReservationsPage'
import { AuthPage } from './components'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [currentPage, setCurrentPage] = useState('booking')

  if (!isAuthenticated) {
    return <AuthPage onLogin={() => setIsAuthenticated(true)} />
  }

  if (currentPage === 'reservations') {
    return (
      <MyReservationsPage
        onNavigate={setCurrentPage}
        onLogout={() => setIsAuthenticated(false)}
      />
    )
  }

  return (
    <BookingPage
      onNavigate={setCurrentPage}
      onLogout={() => setIsAuthenticated(false)}
    />
  )
}

export default App
