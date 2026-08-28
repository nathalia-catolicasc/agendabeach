import { AuthPanel } from '../AuthPanel'
import { AuthShowcase } from '../AuthShowcase'
import './AuthPage.css'

type AuthPageProps = {
  onLogin: () => void
}

export function AuthPage({ onLogin }: AuthPageProps) {
  return (
    <main className="auth-page">
      <AuthShowcase />
      <AuthPanel onLogin={onLogin} />
    </main>
  )
}
