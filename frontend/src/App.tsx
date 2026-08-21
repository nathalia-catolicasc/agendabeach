import { useState } from 'react'
import type { FormEvent } from 'react'
import {
  Anchor,
  Box,
  Button,
  Checkbox,
  Divider,
  Group,
  PasswordInput,
  SegmentedControl,
  Stack,
  Text,
  TextInput,
  ThemeIcon,
  Title,
} from '@mantine/core'
import './App.css'
import BookingPage from './BookingPage'

type AuthMode = 'login' | 'register'

type IconProps = {
  size?: number
  stroke?: number
}

function MailIcon({ size = 18, stroke = 1.8 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 6.5h16v11H4z" stroke="currentColor" strokeWidth={stroke} strokeLinejoin="round" />
      <path d="m5 8 7 5 7-5" stroke="currentColor" strokeWidth={stroke} strokeLinejoin="round" />
    </svg>
  )
}

function UserIcon({ size = 18, stroke = 1.8 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="8" r="3.5" stroke="currentColor" strokeWidth={stroke} />
      <path d="M5.5 20c.7-4 2.8-6 6.5-6s5.8 2 6.5 6" stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" />
    </svg>
  )
}

function LockIcon({ size = 18, stroke = 1.8 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="5" y="10" width="14" height="10" rx="2" stroke="currentColor" strokeWidth={stroke} />
      <path d="M8.5 10V7.5a3.5 3.5 0 0 1 7 0V10" stroke="currentColor" strokeWidth={stroke} />
    </svg>
  )
}

function ArrowIcon({ size = 18, stroke = 2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function CheckIcon({ size = 16, stroke = 2.2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="m5 12 4 4L19 6" stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function BrandLogo() {
  return (
    <div className="brand" aria-label="Agenda Beach">
      <svg className="brand__mark" viewBox="0 0 68 68" role="img" aria-hidden="true">
        <defs>
          <linearGradient id="gold" x1="8" y1="4" x2="58" y2="64" gradientUnits="userSpaceOnUse">
            <stop stopColor="#F4C85B" />
            <stop offset="1" stopColor="#DDA62F" />
          </linearGradient>
        </defs>
        <rect x="6" y="9" width="56" height="54" rx="15" fill="#0B426C" />
        <path d="M6 23c0-7.7 6.3-14 14-14h28c7.7 0 14 6.3 14 14v3H6v-3Z" fill="url(#gold)" />
        <circle cx="21" cy="8" r="2.4" fill="#FFF8E8" />
        <circle cx="48" cy="8" r="2.4" fill="#FFF8E8" />
        <circle cx="50.5" cy="30.5" r="3.5" fill="#E1AD36" />
        <path d="M50.5 27v7M47 30.5h7" stroke="#A77B1F" strokeWidth=".75" opacity=".65" />
        <ellipse cx="34" cy="40" rx="10" ry="13" transform="rotate(-18 34 40)" stroke="#F8F5E9" strokeWidth="2.2" />
        <path d="m37.5 52 3.8 8" stroke="#F8F5E9" strokeWidth="2.2" strokeLinecap="round" />
        <circle cx="31" cy="35" r="1.2" fill="#F8F5E9" />
        <circle cx="36" cy="38" r="1.2" fill="#F8F5E9" />
        <circle cx="31.5" cy="42.5" r="1.2" fill="#F8F5E9" />
        <circle cx="36.5" cy="46" r="1.2" fill="#F8F5E9" />
      </svg>
      <div className="brand__wordmark">
        <div><strong>Agenda</strong><span>Beach</span></div>
        <small>AGENDAMENTO DE QUADRAS</small>
      </div>
    </div>
  )
}

function CourtArtwork() {
  return (
    <div className="court-art" aria-hidden="true">
      <span className="court-art__ball court-art__ball--one" />
      <span className="court-art__ball court-art__ball--two" />
      <div className="court-art__glow" />
      <svg viewBox="0 0 510 390" className="court-art__lines">
        <path d="M96 319 182 92h165l78 227H96Z" fill="rgba(8,53,88,.43)" stroke="rgba(255,255,255,.24)" strokeWidth="2" />
        <path d="m181 92 48 227m118-227-57 227M138 211h247" stroke="rgba(255,255,255,.32)" strokeWidth="2" />
        <path d="M119 263h286" stroke="#E8B441" strokeWidth="4" strokeLinecap="round" />
        <path d="M128 265v-47m268 47v-47" stroke="#E8B441" strokeWidth="5" strokeLinecap="round" />
        <path d="M128 228c90 9 179 9 268 0" stroke="#F2D480" strokeWidth="2" strokeDasharray="5 5" />
        <g transform="translate(322 57) rotate(24)">
          <ellipse cx="34" cy="49" rx="28" ry="39" fill="rgba(232,180,65,.13)" stroke="#E8B441" strokeWidth="7" />
          <path d="M18 21 50 75M8 40l44 20M10 59l40-18M22 11l33 49M17 86l-13 38" stroke="#E8B441" strokeWidth="3" strokeLinecap="round" />
          <path d="M8 122c7 5 11 5 18 0" stroke="#F2D480" strokeWidth="5" strokeLinecap="round" />
        </g>
      </svg>
      <div className="court-art__badge">
        <span>✓</span>
        <div><strong>Reserva confirmada</strong><small>Sua quadra está garantida</small></div>
      </div>
    </div>
  )
}

function LoginForm({ onModeChange, onLogin }: { onModeChange: (mode: AuthMode) => void; onLogin: () => void }) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onLogin()
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="md">
        <TextInput label="E-mail" placeholder="seu@email.com" type="email" required autoComplete="email" leftSection={<MailIcon />} />
        <PasswordInput label="Senha" placeholder="Digite sua senha" required autoComplete="current-password" leftSection={<LockIcon />} />
        <Group justify="space-between" align="center" mt={2}>
          <Checkbox label="Lembrar de mim" color="gold" />
          <Anchor component="button" type="button" size="sm" fw={600} c="brand.8">Esqueci minha senha</Anchor>
        </Group>
        <Button type="submit" fullWidth size="md" color="gold" rightSection={<ArrowIcon />} mt="xs">Entrar na minha conta</Button>
      </Stack>
      <Divider label="ou" labelPosition="center" my="xl" />
      <Text ta="center" size="sm" c="dimmed">
        Ainda não tem uma conta?{' '}
        <Anchor component="button" type="button" fw={700} c="brand.8" onClick={() => onModeChange('register')}>Cadastre-se grátis</Anchor>
      </Text>
    </form>
  )
}

function RegisterForm({ onModeChange }: { onModeChange: (mode: AuthMode) => void }) {
  const [submitted, setSubmitted] = useState(false)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitted(true)
  }

  if (submitted) {
    return (
      <div className="success-state" role="status">
        <ThemeIcon size={62} radius="xl" color="green" variant="light"><CheckIcon size={30} /></ThemeIcon>
        <Title order={3}>Conta criada!</Title>
        <Text c="dimmed" ta="center">Agora é só entrar e escolher a melhor quadra e horário para você.</Text>
        <Button color="gold" fullWidth onClick={() => onModeChange('login')}>Ir para o login</Button>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        <TextInput label="Nome completo" placeholder="Como devemos chamar você?" required autoComplete="name" leftSection={<UserIcon />} />
        <TextInput label="E-mail" placeholder="seu@email.com" type="email" required autoComplete="email" leftSection={<MailIcon />} />
        <PasswordInput label="Senha" description="Use pelo menos 8 caracteres" placeholder="Crie uma senha segura" minLength={8} required autoComplete="new-password" leftSection={<LockIcon />} />
        <Checkbox
          required
          color="gold"
          mt={4}
          label={<Text span size="sm" c="dimmed">Li e aceito os <Anchor href="#" fw={600} c="brand.8">Termos de Uso</Anchor> e a <Anchor href="#" fw={600} c="brand.8">Política de Privacidade</Anchor>.</Text>}
        />
        <Button type="submit" fullWidth size="md" color="gold" rightSection={<ArrowIcon />} mt="xs">Criar minha conta</Button>
      </Stack>
      <Text ta="center" size="sm" c="dimmed" mt="xl">
        Já tem uma conta?{' '}
        <Anchor component="button" type="button" fw={700} c="brand.8" onClick={() => onModeChange('login')}>Fazer login</Anchor>
      </Text>
    </form>
  )
}

function App() {
  const [mode, setMode] = useState<AuthMode>('login')
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  if (isAuthenticated) {
    return <BookingPage onLogout={() => setIsAuthenticated(false)} />
  }

  return (
    <main className="auth-page">
      <section className="showcase">
        <BrandLogo />
        <div className="showcase__content">
          <Text className="showcase__eyebrow">SEU JOGO COMEÇA AQUI</Text>
          <Title className="showcase__title" order={1}>Menos espera.<br />Mais <span>Beach Tennis.</span></Title>
          <Text className="showcase__copy">Encontre sua quadra, escolha o melhor horário e faça sua reserva em poucos minutos.</Text>
          <Group className="showcase__features" gap="xl">
            <div><CheckIcon /><span>Reserva rápida</span></div>
            <div><CheckIcon /><span>Horários em tempo real</span></div>
          </Group>
        </div>
        <CourtArtwork />
        <Text className="showcase__footer">© 2026 Agenda Beach</Text>
      </section>

      <section className="auth-panel">
        <Box className="mobile-brand"><BrandLogo /></Box>
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
          {mode === 'login' ? <LoginForm onModeChange={setMode} onLogin={() => setIsAuthenticated(true)} /> : <RegisterForm onModeChange={setMode} />}
        </div>
        <Text className="auth-panel__footer" size="xs" c="dimmed" ta="center">Precisa de ajuda? <Anchor href="mailto:contato@agendabeach.com.br" c="brand.8" fw={600}>Fale com a gente</Anchor></Text>
      </section>
    </main>
  )
}

export default App
