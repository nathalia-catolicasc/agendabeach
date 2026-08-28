import { useState } from 'react'
import { Anchor, Box, SegmentedControl, Text, Title } from '@mantine/core'
import { LoginForm } from '../LoginForm'
import { Logo } from '../Logo'
import { RegisterForm } from '../RegisterForm'
import './AuthPanel.css'

type AuthMode = 'login' | 'register'

type AuthPanelProps = {
  onLogin: () => void
}

export function AuthPanel({ onLogin }: AuthPanelProps) {
  const [mode, setMode] = useState<AuthMode>('login')
  const showLogin = () => setMode('login')
  const showRegister = () => setMode('register')

  return (
    <section className="auth-panel">
      <Box className="mobile-brand"><Logo showTagline /></Box>
      <div className="auth-panel__top"><Anchor href="#" c="dimmed" size="sm">← Voltar para o início</Anchor></div>

      <div className="auth-card">
        <div className="auth-card__heading">
          <Text className="auth-card__kicker">BEM-VINDO À AGENDA BEACH</Text>
          <Title order={2}>{mode === 'login' ? 'Entre na sua conta' : 'Crie sua conta'}</Title>
          <Text c="dimmed">{mode === 'login' ? 'Acesse suas reservas e volte para a quadra.' : 'Cadastre-se para agendar sua próxima partida.'}</Text>
        </div>

        <SegmentedControl
          className="auth-switch"
          fullWidth
          value={mode}
          onChange={(value) => setMode(value as AuthMode)}
          data={[{ label: 'Entrar', value: 'login' }, { label: 'Criar conta', value: 'register' }]}
        />

        {mode === 'login'
          ? <LoginForm onLogin={onLogin} onShowRegister={showRegister} />
          : <RegisterForm onShowLogin={showLogin} />}
      </div>

      <Text className="auth-panel__footer" size="xs" c="dimmed" ta="center">
        Precisa de ajuda? <Anchor href="mailto:contato@agendabeach.com.br" c="brand.8" fw={600}>Fale com a gente</Anchor>
      </Text>
    </section>
  )
}
