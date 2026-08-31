import { useMemo, useState } from 'react'
import { AppHeader, ConfirmDialog, PageHeading, ReservationCard, getReservationCosts } from './components'
import type { Reservation, ReservationStatus } from './components'
import { navigationItems } from './navigation'
import './MyReservationsPage.css'

type Filter = ReservationStatus | 'all'

type MyReservationsPageProps = {
  onNavigate: (value: string) => void
  onLogout: () => void
}

const filters: { label: string; value: Filter }[] = [
  { label: 'Próximas', value: 'upcoming' },
  { label: 'Realizadas', value: 'completed' },
  { label: 'Canceladas', value: 'cancelled' },
  { label: 'Todas', value: 'all' },
]

const emptyMessages: Record<Filter, string> = {
  upcoming: 'Você não tem nenhuma partida agendada. Escolha um dia no calendário para reservar sua quadra.',
  completed: 'Nenhuma partida realizada por aqui ainda.',
  cancelled: 'Nenhuma reserva cancelada. Continue assim!',
  all: 'Você ainda não fez nenhuma reserva.',
}

const initialReservations: Reservation[] = [
  {
    id: 'ab-0184',
    code: 'AB-2026-0184',
    court: 'Quadra 1 · Arena Sunset',
    surface: 'Areia oficial · Coberta',
    date: new Date(2026, 5, 18),
    startTime: '19:00',
    hours: 1.5,
    pricePerHour: 120,
    equipments: [
      { name: 'Par de raquetes', quantity: 2, price: 25 },
      { name: 'Iluminação noturna', quantity: 1, price: 40 },
    ],
    status: 'upcoming',
  },
  {
    id: 'ab-0179',
    code: 'AB-2026-0179',
    court: 'Quadra 4 · Vôlei Pro',
    surface: 'Areia oficial · Ao ar livre',
    date: new Date(2026, 5, 22),
    startTime: '08:00',
    hours: 2,
    pricePerHour: 140,
    equipments: [
      { name: 'Bola oficial', quantity: 1, price: 15 },
      { name: 'Kit de coletes', quantity: 2, price: 10 },
    ],
    status: 'upcoming',
  },
  {
    id: 'ab-0166',
    code: 'AB-2026-0166',
    court: 'Quadra 2 · Beira Mar',
    surface: 'Areia fina · Ao ar livre',
    date: new Date(2026, 5, 29),
    startTime: '16:00',
    hours: 1,
    pricePerHour: 100,
    equipments: [],
    status: 'upcoming',
  },
  {
    id: 'ab-0142',
    code: 'AB-2026-0142',
    court: 'Quadra 3 · Beach Tennis Center',
    surface: 'Areia compactada · Coberta',
    date: new Date(2026, 5, 6),
    startTime: '18:00',
    hours: 1.5,
    pricePerHour: 110,
    equipments: [{ name: 'Par de raquetes', quantity: 2, price: 25 }],
    status: 'completed',
  },
  {
    id: 'ab-0128',
    code: 'AB-2026-0128',
    court: 'Quadra 1 · Arena Sunset',
    surface: 'Areia oficial · Coberta',
    date: new Date(2026, 4, 24),
    startTime: '10:00',
    hours: 2,
    pricePerHour: 120,
    equipments: [
      { name: 'Cooler com gelo', quantity: 1, price: 20 },
      { name: 'Bola oficial', quantity: 1, price: 15 },
    ],
    status: 'completed',
  },
  {
    id: 'ab-0117',
    code: 'AB-2026-0117',
    court: 'Quadra 2 · Beira Mar',
    surface: 'Areia fina · Ao ar livre',
    date: new Date(2026, 4, 11),
    startTime: '17:00',
    hours: 1,
    pricePerHour: 100,
    equipments: [],
    status: 'cancelled',
  },
]

const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function formatHours(hours: number) {
  return Number.isInteger(hours) ? `${hours}h` : `${hours.toString().replace('.', ',')}h`
}

export default function MyReservationsPage({ onNavigate, onLogout }: MyReservationsPageProps) {
  const [reservations, setReservations] = useState(initialReservations)
  const [activeFilter, setActiveFilter] = useState<Filter>('upcoming')
  const [expandedId, setExpandedId] = useState<string | null>(null)
  const [reservationToCancel, setReservationToCancel] = useState<Reservation | null>(null)

  const counts = useMemo(() => ({
    upcoming: reservations.filter((reservation) => reservation.status === 'upcoming').length,
    completed: reservations.filter((reservation) => reservation.status === 'completed').length,
    cancelled: reservations.filter((reservation) => reservation.status === 'cancelled').length,
    all: reservations.length,
  }), [reservations])

  const stats = useMemo(() => {
    const active = reservations.filter((reservation) => reservation.status !== 'cancelled')
    const playedHours = reservations
      .filter((reservation) => reservation.status === 'completed')
      .reduce((total, reservation) => total + reservation.hours, 0)
    const invested = active.reduce((total, reservation) => total + getReservationCosts(reservation).total, 0)

    return { playedHours, invested }
  }, [reservations])

  const visibleReservations = useMemo(() => {
    const filtered = activeFilter === 'all'
      ? reservations
      : reservations.filter((reservation) => reservation.status === activeFilter)

    return [...filtered].sort((first, second) => (
      first.status === 'upcoming' && second.status === 'upcoming'
        ? first.date.getTime() - second.date.getTime()
        : second.date.getTime() - first.date.getTime()
    ))
  }, [reservations, activeFilter])

  function toggleDetails(id: string) {
    setExpandedId((current) => (current === id ? null : id))
  }

  function cancelReservation() {
    if (!reservationToCancel) return

    setReservations((current) => current.map((reservation) => (
      reservation.id === reservationToCancel.id ? { ...reservation, status: 'cancelled' } : reservation
    )))
    setReservationToCancel(null)
  }

  return (
    <div className="reservations-page">
      <AppHeader
        activeItem="reservations"
        navigationItems={navigationItems}
        onNavigate={onNavigate}
        onAvatarClick={onLogout}
      />

      <main className="reservations-main">
        <div className="reservations-heading">
          <PageHeading
            eyebrow="MINHA AGENDA"
            title="Meus agendamentos"
            description="Acompanhe suas próximas partidas, consulte o histórico e cancele quando precisar."
          />
          <button type="button" className="reservations-new" onClick={() => onNavigate('booking')}>
            Agendar nova partida
          </button>
        </div>

        <div className="reservations-stats">
          <div className="reservations-stat">
            <span>PRÓXIMAS PARTIDAS</span>
            <strong>{counts.upcoming}</strong>
          </div>
          <div className="reservations-stat">
            <span>PARTIDAS REALIZADAS</span>
            <strong>{counts.completed}</strong>
          </div>
          <div className="reservations-stat">
            <span>HORAS EM QUADRA</span>
            <strong>{formatHours(stats.playedHours)}</strong>
          </div>
          <div className="reservations-stat">
            <span>TOTAL INVESTIDO</span>
            <strong>{currencyFormatter.format(stats.invested)}</strong>
          </div>
        </div>

        <div className="reservations-filters" role="group" aria-label="Filtrar reservas">
          {filters.map(({ label, value }) => (
            <button
              key={value}
              type="button"
              className={`reservations-filter${value === activeFilter ? ' reservations-filter--active' : ''}`}
              onClick={() => setActiveFilter(value)}
              aria-pressed={value === activeFilter}
            >
              {label}
              <i>{counts[value]}</i>
            </button>
          ))}
        </div>

        {visibleReservations.length > 0 ? (
          <div className="reservations-list">
            {visibleReservations.map((reservation) => (
              <ReservationCard
                key={reservation.id}
                reservation={reservation}
                expanded={reservation.id === expandedId}
                onToggleDetails={() => toggleDetails(reservation.id)}
                onCancel={() => setReservationToCancel(reservation)}
                onRebook={() => onNavigate('booking')}
              />
            ))}
          </div>
        ) : (
          <div className="reservations-empty">
            <p>{emptyMessages[activeFilter]}</p>
            <button type="button" onClick={() => onNavigate('booking')}>Ir para o calendário</button>
          </div>
        )}
      </main>

      {reservationToCancel && (
        <ConfirmDialog
          eyebrow="CANCELAR RESERVA"
          title={reservationToCancel.court}
          description={(
            <>
              <p>
                Tem certeza que deseja cancelar a reserva <strong>{reservationToCancel.code}</strong> do dia{' '}
                <strong>{new Intl.DateTimeFormat('pt-BR').format(reservationToCancel.date)}</strong> às{' '}
                <strong>{reservationToCancel.startTime}</strong>?
              </p>
              <p>Cancelamentos feitos até 12 horas antes do início da partida não têm custo.</p>
            </>
          )}
          confirmLabel="Cancelar reserva"
          dismissLabel="Manter reserva"
          onConfirm={cancelReservation}
          onClose={() => setReservationToCancel(null)}
        />
      )}
    </div>
  )
}
