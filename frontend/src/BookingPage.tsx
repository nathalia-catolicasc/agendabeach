import { useState } from 'react'
import './BookingPage.css'

type Availability = 'available' | 'limited' | 'full'

type CalendarDay = {
  day: number
  status?: Availability
  disabled?: boolean
}

const weekdays = ['DOM', 'SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB']

const availabilityLabels: Record<Availability, string> = {
  available: 'Vários',
  limited: 'Poucos',
  full: 'Lotado',
}

const calendarDays: CalendarDay[] = Array.from({ length: 30 }, (_, index) => {
  const day = index + 1

  if (day < 14) return { day, disabled: true }
  if (day === 18 || day === 23) return { day, status: 'full', disabled: true }
  if ([16, 20, 25, 29].includes(day)) return { day, status: 'limited' }
  return { day, status: 'available' }
})

function ChevronIcon({ direction }: { direction: 'left' | 'right' }) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d={direction === 'left' ? 'm14.5 6-6 6 6 6' : 'm9.5 6 6 6-6 6'} />
    </svg>
  )
}

function CalendarIcon() {
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true">
      <rect x="7" y="10" width="34" height="32" rx="6" />
      <path d="M7 19h34M16 6v8M32 6v8" />
      <path className="calendar-icon__accent" d="M17 26h4v4h-4zM27 26h4v4h-4zM17 34h4v4h-4zM27 34h4v4h-4z" />
    </svg>
  )
}

function HeaderLogo() {
  return (
    <div className="booking-logo" aria-label="Agenda Beach">
      <svg viewBox="0 0 68 68" aria-hidden="true">
        <defs>
          <linearGradient id="booking-gold" x1="8" y1="4" x2="58" y2="64" gradientUnits="userSpaceOnUse">
            <stop stopColor="#f6cf68" />
            <stop offset="1" stopColor="#dfa72c" />
          </linearGradient>
        </defs>
        <rect x="6" y="9" width="56" height="54" rx="15" fill="#0b426c" />
        <path d="M6 23c0-7.7 6.3-14 14-14h28c7.7 0 14 6.3 14 14v3H6v-3Z" fill="url(#booking-gold)" />
        <ellipse cx="34" cy="40" rx="10" ry="13" transform="rotate(-18 34 40)" fill="none" stroke="#fff9ec" strokeWidth="2.4" />
        <path d="m37.5 52 3.8 8" stroke="#fff9ec" strokeWidth="2.4" strokeLinecap="round" />
        <circle cx="31" cy="35" r="1.3" fill="#fff9ec" />
        <circle cx="36" cy="39" r="1.3" fill="#fff9ec" />
        <circle cx="32" cy="44" r="1.3" fill="#fff9ec" />
      </svg>
      <span>Agenda<strong>Beach</strong></span>
    </div>
  )
}

type BookingPageProps = {
  onLogout: () => void
}

export default function BookingPage({ onLogout }: BookingPageProps) {
  const [selectedDay, setSelectedDay] = useState<number | null>(null)

  return (
    <div className="booking-page">
      <header className="booking-header">
        <HeaderLogo />
        <nav className="booking-nav" aria-label="Navegação principal">
          <button type="button">Início</button>
          <button type="button" className="booking-nav__active">Agendar</button>
          <button type="button">Minhas reservas</button>
          <button type="button">Quadras</button>
        </nav>
        <button type="button" className="booking-avatar" onClick={onLogout} title="Sair da conta" aria-label="Sair da conta">AB</button>
      </header>

      <main className="booking-main">
        <div className="booking-intro">
          <p className="booking-intro__eyebrow">RESERVAS ONLINE</p>
          <h1>Agendar quadra</h1>
          <p>Toque em um dia disponível no calendário para escolher a quadra e o horário.</p>
        </div>

        <div className="booking-layout">
          <section className="calendar-card" aria-labelledby="calendar-title">
            <div className="booking-step">
              <span>1</span>
              <strong id="calendar-title">SELECIONE O DIA</strong>
            </div>

            <div className="calendar-toolbar">
              <h2>Junho 2026</h2>
              <div>
                <button type="button" aria-label="Mês anterior"><ChevronIcon direction="left" /></button>
                <button type="button" aria-label="Próximo mês"><ChevronIcon direction="right" /></button>
              </div>
            </div>

            <div className="calendar-grid calendar-grid--weekdays" aria-hidden="true">
              {weekdays.map((weekday) => <span key={weekday}>{weekday}</span>)}
            </div>

            <div className="calendar-grid calendar-grid--days">
              <span className="calendar-spacer" aria-hidden="true" />
              {calendarDays.map(({ day, status, disabled }) => (
                <button
                  key={day}
                  type="button"
                  className={`calendar-day${disabled ? ' calendar-day--disabled' : ''}${status === 'full' ? ' calendar-day--full' : ''}${selectedDay === day ? ' calendar-day--selected' : ''}`}
                  disabled={disabled}
                  onClick={() => setSelectedDay(day)}
                  aria-pressed={selectedDay === day}
                  aria-label={`${day} de junho${status ? `, ${availabilityLabels[status]} horários` : ''}`}
                >
                  <strong>{day}</strong>
                  {status && <span className={`availability availability--${status}`}>{availabilityLabels[status]}</span>}
                </button>
              ))}
            </div>

            <div className="calendar-legend" aria-label="Legenda de disponibilidade">
              <span><i className="legend-dot legend-dot--available" />Vários horários</span>
              <span><i className="legend-dot legend-dot--limited" />Poucos horários</span>
              <span><i className="legend-dot legend-dot--full" />Lotado</span>
            </div>
          </section>

          <aside className="booking-summary" aria-live="polite">
            <div className="booking-summary__header">
              <h2>Resumo da reserva</h2>
              <p>{selectedDay ? 'Continue preenchendo as etapas' : 'Preencha as etapas ao lado'}</p>
            </div>
            <div className="booking-summary__body">
              <div className="booking-summary__icon"><CalendarIcon /></div>
              {selectedDay ? (
                <>
                  <span className="booking-summary__label">DATA SELECIONADA</span>
                  <strong>{selectedDay} de junho de 2026</strong>
                  <p>Agora escolha a quadra e o melhor horário para sua partida.</p>
                  <button type="button">Escolher quadra e horário</button>
                </>
              ) : (
                <p>Nenhum dia selecionado.<br />Comece escolhendo uma data no calendário.</p>
              )}
            </div>
          </aside>
        </div>
      </main>
    </div>
  )
}
