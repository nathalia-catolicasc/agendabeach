import { useCallback, useEffect, useMemo, useState } from 'react'
import { AppHeader, ConfirmDialog, PageHeading } from './components'
import { getNavigationItems } from './navigation'
import { ApiError, cancelBooking, listBookings, listCourts, listUserBookings, listUsers } from './api'
import type { ApiUser, Booking, Cancellation, Court } from './api'
import { isAdmin, getInitials } from './session'
import type { SessionUser } from './session'
import {
  formatCurrency,
  formatDuration,
  formatFullDate,
  formatShortDate,
  formatTime,
  getHoursBetween,
  parseLocalDateTime,
} from './datetime'
import { checkCancellation, getRefundPolicy } from './pricing'
import './CancelBookingPage.css'

type CancelBookingPageProps = {
  user: SessionUser
  onNavigate: (value: string) => void
  onLogout: () => void
}

type Scope = 'mine' | 'all'

export default function CancelBookingPage({ user, onNavigate, onLogout }: CancelBookingPageProps) {
  const isAdminUser = isAdmin(user)

  const [scope, setScope] = useState<Scope>('mine')
  const [bookings, setBookings] = useState<Booking[]>([])
  const [courts, setCourts] = useState<Court[]>([])
  const [users, setUsers] = useState<ApiUser[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [confirming, setConfirming] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [result, setResult] = useState<Cancellation | null>(null)

  const loadData = useCallback(
    () =>
      Promise.all([
        scope === 'all' ? listBookings() : listUserBookings(user.id),
        listCourts(),
        scope === 'all' ? listUsers() : Promise.resolve<ApiUser[]>([]),
      ])
        .then(([loadedBookings, loadedCourts, loadedUsers]) => {
          setBookings(loadedBookings)
          setCourts(loadedCourts)
          setUsers(loadedUsers)
          setLoadError(null)
        })
        .catch((caught: unknown) => {
          setLoadError(caught instanceof ApiError ? caught.message : 'Não foi possível carregar suas reservas.')
        })
        .finally(() => setLoading(false)),
    [scope, user.id],
  )

  useEffect(() => {
    loadData()
  }, [loadData])

  const courtsById = useMemo(() => new Map(courts.map((court) => [court.id, court])), [courts])
  const usersById = useMemo(() => new Map(users.map((item) => [item.id, item])), [users])

  const cancellable = useMemo(() => {
    const now = new Date()

    return bookings
      .filter((booking) => checkCancellation(booking, now).allowed)
      .sort(
        (first, second) =>
          parseLocalDateTime(first.startTime).getTime() - parseLocalDateTime(second.startTime).getTime(),
      )
  }, [bookings])

  const selected = useMemo(
    () => cancellable.find((booking) => booking.id === selectedId) ?? null,
    [cancellable, selectedId],
  )

  function selectBooking(id: number) {
    setSelectedId(id)
    setActionError(null)
    setResult(null)
  }

  function changeScope(next: Scope) {
    setLoading(true)
    setScope(next)
    setSelectedId(null)
    setResult(null)
    setActionError(null)
  }

  async function confirmCancellation() {
    if (!selected) return

    setSubmitting(true)
    setActionError(null)

    try {
      const cancellation = await cancelBooking(selected.id)
      setResult(cancellation)
      setConfirming(false)
      setSelectedId(null)
      await loadData()
    } catch (caught) {
      setConfirming(false)
      setActionError(
        caught instanceof ApiError && caught.status === 0
          ? caught.message
          : 'Não foi possível cancelar esta reserva. Ela pode já ter começado ou sido cancelada.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  function getCourtName(courtId: number) {
    return courtsById.get(courtId)?.name ?? `Quadra #${courtId}`
  }

  function getCourtSurface(courtId: number) {
    const court = courtsById.get(courtId)
    if (!court) return 'Quadra de areia'
    return court.type === 'COVERED' ? 'Areia oficial · Coberta' : 'Areia oficial · Ao ar livre'
  }

  const selectedStart = selected ? parseLocalDateTime(selected.startTime) : null
  const selectedEnd = selected ? parseLocalDateTime(selected.endTime) : null
  const policy = selectedStart ? getRefundPolicy(selectedStart) : null
  const estimatedRefund = selected && policy ? selected.courtPrice * policy.rate : 0

  return (
    <div className="cancel-page">
      <AppHeader
        activeItem="cancel"
        navigationItems={getNavigationItems(user)}
        onNavigate={onNavigate}
        onAvatarClick={onLogout}
        avatarLabel={getInitials(user.name)}
      />

      <main className="cancel-main">
        <div className="cancel-heading">
          <PageHeading
            eyebrow="CANCELAMENTO"
            title="Cancelar reserva de quadra"
            description="Escolha a reserva que você não vai usar, confira a política de estorno e confirme o cancelamento."
          />

          {isAdminUser && (
            <div className="cancel-scope" role="group" aria-label="Origem das reservas">
              <button
                type="button"
                className={scope === 'mine' ? 'cancel-scope--active' : undefined}
                onClick={() => changeScope('mine')}
                aria-pressed={scope === 'mine'}
              >
                Minhas reservas
              </button>
              <button
                type="button"
                className={scope === 'all' ? 'cancel-scope--active' : undefined}
                onClick={() => changeScope('all')}
                aria-pressed={scope === 'all'}
              >
                Todas as reservas
              </button>
            </div>
          )}
        </div>

        {result && (
          <div className="cancel-banner cancel-banner--success" role="status">
            <strong>Reserva #{result.bookingId} cancelada.</strong>
            <span>
              Valor de estorno confirmado pelo sistema: <b>{formatCurrency(result.refundAmount)}</b>
              {result.cancelledAt &&
                ` · cancelada em ${formatShortDate(parseLocalDateTime(result.cancelledAt))} às ${formatTime(parseLocalDateTime(result.cancelledAt))}`}
            </span>
            <button type="button" onClick={() => onNavigate('reservations')}>
              Ver minhas reservas
            </button>
          </div>
        )}

        {actionError && (
          <div className="cancel-banner cancel-banner--error" role="alert">
            {actionError}
          </div>
        )}
        {loadError && (
          <div className="cancel-banner cancel-banner--error" role="alert">
            {loadError}
          </div>
        )}

        <div className="cancel-layout">
          <section className="cancel-list" aria-label="Reservas que podem ser canceladas">
            <header className="cancel-list__header">
              <h2>Reservas ativas</h2>
              <span>{cancellable.length} disponível(is) para cancelamento</span>
            </header>

            {loading && <p className="cancel-empty">Carregando reservas…</p>}

            {!loading && cancellable.length === 0 && (
              <div className="cancel-empty">
                <p>
                  Nenhuma reserva pode ser cancelada agora. Só é possível cancelar partidas agendadas que ainda não
                  começaram.
                </p>
                <button type="button" onClick={() => onNavigate('booking')}>
                  Agendar nova partida
                </button>
              </div>
            )}

            {!loading &&
              cancellable.map((booking) => {
                const start = parseLocalDateTime(booking.startTime)
                const end = parseLocalDateTime(booking.endTime)
                const itemPolicy = getRefundPolicy(start)
                const owner = usersById.get(booking.userId)

                return (
                  <button
                    key={booking.id}
                    type="button"
                    className={`cancel-item${booking.id === selectedId ? ' cancel-item--selected' : ''}`}
                    onClick={() => selectBooking(booking.id)}
                    aria-pressed={booking.id === selectedId}
                  >
                    <span className="cancel-item__date">
                      <small>
                        {new Intl.DateTimeFormat('pt-BR', { month: 'short' })
                          .format(start)
                          .replace('.', '')
                          .toUpperCase()}
                      </small>
                      <strong>{start.getDate()}</strong>
                    </span>

                    <span className="cancel-item__info">
                      <strong>{getCourtName(booking.courtId)}</strong>
                      <small>{getCourtSurface(booking.courtId)}</small>
                      <small>
                        {formatTime(start)} às {formatTime(end)} · {formatDuration(getHoursBetween(start, end))}
                      </small>
                      {scope === 'all' && (
                        <small className="cancel-item__owner">
                          {owner ? owner.name : `Usuário #${booking.userId}`}
                        </small>
                      )}
                    </span>

                    <span className="cancel-item__aside">
                      <b>{formatCurrency(booking.courtPrice)}</b>
                      <i
                        className={`cancel-tag cancel-tag--${itemPolicy.rate === 1 ? 'full' : itemPolicy.rate === 0.5 ? 'half' : 'none'}`}
                      >
                        {itemPolicy.label}
                      </i>
                    </span>
                  </button>
                )
              })}
          </section>

          <aside className="cancel-detail" aria-live="polite">
            {selected && selectedStart && selectedEnd && policy ? (
              <>
                <h2>Confira antes de confirmar</h2>

                <dl className="cancel-specs">
                  <div>
                    <dt>Quadra</dt>
                    <dd>{getCourtName(selected.courtId)}</dd>
                  </div>
                  <div>
                    <dt>Data</dt>
                    <dd>{formatFullDate(selectedStart)}</dd>
                  </div>
                  <div>
                    <dt>Horário</dt>
                    <dd>
                      {formatTime(selectedStart)} às {formatTime(selectedEnd)}
                    </dd>
                  </div>
                  <div>
                    <dt>Duração</dt>
                    <dd>{formatDuration(getHoursBetween(selectedStart, selectedEnd))}</dd>
                  </div>
                  <div>
                    <dt>Código</dt>
                    <dd>AB-{String(selected.id).padStart(4, '0')}</dd>
                  </div>
                </dl>

                <div
                  className={`cancel-policy cancel-policy--${policy.rate === 1 ? 'full' : policy.rate === 0.5 ? 'half' : 'none'}`}
                >
                  <strong>{policy.label}</strong>
                  <p>{policy.description}</p>
                </div>

                <ul className="cancel-costs">
                  <li>
                    <span>Valor da reserva</span>
                    <strong>{formatCurrency(selected.courtPrice)}</strong>
                  </li>
                  <li>
                    <span>Estorno previsto</span>
                    <strong>{formatCurrency(estimatedRefund)}</strong>
                  </li>
                </ul>

                <button
                  type="button"
                  className="cancel-submit"
                  onClick={() => setConfirming(true)}
                  disabled={submitting}
                >
                  {submitting ? 'Cancelando…' : 'Cancelar esta reserva'}
                </button>

                <p className="cancel-note">
                  O valor final do estorno é calculado pelo servidor no momento do cancelamento e pode variar se o
                  horário se aproximar.
                </p>
              </>
            ) : (
              <div className="cancel-detail__empty">
                <h2>Nenhuma reserva selecionada</h2>
                <p>Escolha uma reserva na lista ao lado para ver a política de estorno e confirmar o cancelamento.</p>
              </div>
            )}
          </aside>
        </div>
      </main>

      {confirming && selected && selectedStart && (
        <ConfirmDialog
          eyebrow="CANCELAR RESERVA"
          title={getCourtName(selected.courtId)}
          description={
            <>
              <p>
                Confirma o cancelamento da reserva <strong>AB-{String(selected.id).padStart(4, '0')}</strong> do dia{' '}
                <strong>{formatShortDate(selectedStart)}</strong> às <strong>{formatTime(selectedStart)}</strong>?
              </p>
              <p>
                {policy?.description} Estorno previsto: <strong>{formatCurrency(estimatedRefund)}</strong>.
              </p>
            </>
          }
          confirmLabel={submitting ? 'Cancelando…' : 'Sim, cancelar'}
          dismissLabel="Manter reserva"
          onConfirm={() => void confirmCancellation()}
          onClose={() => setConfirming(false)}
        />
      )}
    </div>
  )
}
