import { getReservationCosts } from './getReservationCosts'
import './ReservationCard.css'

export type ReservationStatus = 'upcoming' | 'completed' | 'cancelled'

export type ReservationEquipment = {
  name: string
  quantity: number
  price: number
}

export type Reservation = {
  id: string
  code: string
  court: string
  surface: string
  date: Date
  startTime: string
  hours: number
  pricePerHour: number
  equipments: ReservationEquipment[]
  status: ReservationStatus
}

type ReservationCardProps = {
  reservation: Reservation
  expanded: boolean
  onToggleDetails: () => void
  onCancel?: () => void
  onRebook?: () => void
}

const statusLabels: Record<ReservationStatus, string> = {
  upcoming: 'Confirmada',
  completed: 'Realizada',
  cancelled: 'Cancelada',
}

const durationLabels: Record<string, string> = {
  '1': '1 hora',
  '1.5': '1h30',
  '2': '2 horas',
}

const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const monthFormatter = new Intl.DateTimeFormat('pt-BR', { month: 'short' })
const weekdayFormatter = new Intl.DateTimeFormat('pt-BR', { weekday: 'short' })

function formatPrice(value: number) {
  return currencyFormatter.format(value)
}

function getShortLabel(formatter: Intl.DateTimeFormat, date: Date) {
  return formatter.format(date).replace('.', '')
}

function getFullDateLabel(date: Date) {
  return new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date)
}

function getDurationLabel(hours: number) {
  return durationLabels[String(hours)] ?? `${hours} horas`
}

function toMinutes(time: string) {
  const [hours, minutes] = time.split(':').map(Number)
  return hours * 60 + minutes
}

function toTimeLabel(totalMinutes: number) {
  const hours = String(Math.floor(totalMinutes / 60)).padStart(2, '0')
  const minutes = String(totalMinutes % 60).padStart(2, '0')
  return `${hours}:${minutes}`
}

function ClockIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </svg>
  )
}

function CourtIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <rect x="3.5" y="6" width="17" height="12" rx="2" />
      <path d="M12 6v12M3.5 12h17" />
    </svg>
  )
}

function ChevronIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="m6 9.5 6 6 6-6" />
    </svg>
  )
}

export function ReservationCard({
  reservation,
  expanded,
  onToggleDetails,
  onCancel,
  onRebook,
}: ReservationCardProps) {
  const { courtTotal, equipmentTotal, serviceFee, total } = getReservationCosts(reservation)
  const endTime = toTimeLabel(toMinutes(reservation.startTime) + reservation.hours * 60)
  const detailsId = `reservation-details-${reservation.id}`

  return (
    <article className={`reservation-card reservation-card--${reservation.status}`}>
      <div className="reservation-card__main">
        <div className="reservation-card__date">
          <span>{getShortLabel(monthFormatter, reservation.date).toUpperCase()}</span>
          <strong>{reservation.date.getDate()}</strong>
          <small>{getShortLabel(weekdayFormatter, reservation.date)}</small>
        </div>

        <div className="reservation-card__info">
          <div className="reservation-card__title">
            <h3>{reservation.court}</h3>
            <span className={`reservation-status reservation-status--${reservation.status}`}>
              {statusLabels[reservation.status]}
            </span>
          </div>

          <p className="reservation-card__surface">{reservation.surface}</p>

          <ul className="reservation-card__meta">
            <li><ClockIcon />{reservation.startTime} às {endTime}</li>
            <li><CourtIcon />{getDurationLabel(reservation.hours)}</li>
          </ul>

          {reservation.equipments.length > 0 && (
            <ul className="reservation-card__equipments">
              {reservation.equipments.map(({ name, quantity }) => (
                <li key={name}>{quantity}× {name}</li>
              ))}
            </ul>
          )}
        </div>

        <div className="reservation-card__aside">
          <span className="reservation-card__code">{reservation.code}</span>
          <span className="reservation-card__total-label">TOTAL</span>
          <strong className="reservation-card__total">{formatPrice(total)}</strong>

          <div className="reservation-card__actions">
            <button
              type="button"
              className={`reservation-card__details-toggle${expanded ? ' reservation-card__details-toggle--open' : ''}`}
              onClick={onToggleDetails}
              aria-expanded={expanded}
              aria-controls={detailsId}
            >
              Detalhes
              <ChevronIcon />
            </button>

            {reservation.status === 'upcoming' && onCancel && (
              <button type="button" className="reservation-card__cancel" onClick={onCancel}>
                Cancelar
              </button>
            )}

            {reservation.status !== 'upcoming' && onRebook && (
              <button type="button" className="reservation-card__rebook" onClick={onRebook}>
                Reservar de novo
              </button>
            )}
          </div>
        </div>
      </div>

      {expanded && (
        <div className="reservation-card__details" id={detailsId}>
          <dl className="reservation-card__specs">
            <div>
              <dt>Data</dt>
              <dd>{getFullDateLabel(reservation.date)}</dd>
            </div>
            <div>
              <dt>Horário</dt>
              <dd>{reservation.startTime} às {endTime}</dd>
            </div>
            <div>
              <dt>Código da reserva</dt>
              <dd>{reservation.code}</dd>
            </div>
            <div>
              <dt>Equipamentos</dt>
              <dd>
                {reservation.equipments.length === 0
                  ? <em>Nenhum</em>
                  : reservation.equipments.map(({ name, quantity }) => (
                    <span key={name}>{quantity}× {name}</span>
                  ))}
              </dd>
            </div>
          </dl>

          <ul className="reservation-card__costs">
            <li>
              <span>Quadra ({getDurationLabel(reservation.hours)})</span>
              <strong>{formatPrice(courtTotal)}</strong>
            </li>
            <li>
              <span>Equipamentos</span>
              <strong>{formatPrice(equipmentTotal)}</strong>
            </li>
            <li>
              <span>Taxa de serviço (5%)</span>
              <strong>{formatPrice(serviceFee)}</strong>
            </li>
            <li>
              <span>Total</span>
              <strong>{formatPrice(total)}</strong>
            </li>
          </ul>
        </div>
      )}
    </article>
  )
}
