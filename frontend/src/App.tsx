import { useState } from 'react'
import BookingPage from './BookingPage'
import { AuthPage } from './components'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  if (isAuthenticated) {
    return <BookingPage onLogout={() => setIsAuthenticated(false)} />
  }

  return <AuthPage onLogin={() => setIsAuthenticated(true)} />
}

export default App
