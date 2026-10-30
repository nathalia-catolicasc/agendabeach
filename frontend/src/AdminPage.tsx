import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { AppHeader, ConfirmDialog, PageHeading } from './components'
import { getNavigationItems } from './navigation'
import {
  ApiError,
  cancelBooking,
  createCourt,
  deleteCourt,
  listBookings,
  listCourts,
  listUsers,
  updateCourt,
} from './api'
import type { ApiUser, Booking, BookingStatus, Court, CourtType } from './api'
import { canTryAdmin, getInitials } from './session'
import type { SessionUser } from './session'
import {
  formatCurrency,
  formatDuration,
  formatShortDate,
  formatTime,
  getHoursBetween,
  parseLocalDateTime,
  toDateString,
} from './datetime'
import { checkCancellation } from './pricing'
import './AdminPage.css'

type AdminPageProps = {
  user: SessionUser
  onNavigate: (value: string) => void
  onLogout: () => void
}

type Tab = 'bookings' | 'courts' | 'users'

type PendingAction = { type: 'cancel-booking'; booking: Booking } | { type: 'delete-court'; court: Court }

const tabs: { label: string; value: Tab }[] = [
  { label: 'Reservas', value: 'bookings' },
  { label: 'Quadras', value: 'courts' },
  { label: 'Clientes', value: 'users' },
]

const statusLabels: Record<BookingStatus, string> = {
  SCHEDULED: 'Agendada',
  CANCELLED: 'Cancelada',
  COMPLETED: 'Realizada',
}

const courtTypeLabels: Record<CourtType, string> = {
  COVERED: 'Coberta',
  OPEN: 'Ao ar livre',
}

export default function AdminPage({ user, onNavigate, onLogout }: AdminPageProps) {
  const [tab, setTab] = useState<Tab>('bookings')
  const [bookings, setBookings] = useState<Booking[]>([])
  const [courts, setCourts] = useState<Court[]>([])
  const [users, setUsers] = useState<ApiUser[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [feedback, setFeedback] = useState<{ tone: 'ok' | 'error'; message: string } | null>(null)
  const [pending, setPending] = useState<PendingAction | null>(null)
  const [busy, setBusy] = useState(false)

  const [statusFilter, setStatusFilter] = useState<BookingStatus | 'ALL'>('ALL')
  const [courtFilter, setCourtFilter] = useState<'ALL' | number>('ALL')
  const [dateFilter, setDateFilter] = useState('')
  const [search, setSearch] = useState('')

  const [courtName, setCourtName] = useState('')
  const [courtType, setCourtType] = useState<CourtType>('OPEN')

  const allowed = canTryAdmin(user)

  const loadData = useCallback(
    () =>
      Promise.all([listBookings(), listCourts(), listUsers()])
        .then(([loadedBookings, loadedCourts, loadedUsers]) => {
          setBookings(loadedBookings)
          setCourts(loadedCourts)
          setUsers(loadedUsers)
          setLoadError(null)
        })
        .catch((caught: unknown) => {
          setLoadError(
            caught instanceof ApiError ? caught.message : 'Não foi possível carregar os dados administrativos.',
          )
        })
        .finally(() => setLoading(false)),
    [],
  )

  useEffect(() => {
    if (allowed) loadData()
  }, [allowed, loadData])

  const courtsById = useMemo(() => new Map(courts.map((court) => [court.id, court])), [courts])
  const usersById = useMemo(() => new Map(users.map((item) => [item.id, item])), [users])

  const stats = useMemo(() => {
    const scheduled = bookings.filter((booking) => booking.status === 'SCHEDULED')
    const cancelled = bookings.filter((booking) => booking.status === 'CANCELLED')
    const revenue = bookings
      .filter((booking) => booking.status !== 'CANCELLED')
      .reduce((total, booking) => total + booking.courtPrice, 0)

    return {
      total: bookings.length,
      scheduled: scheduled.length,
      cancelled: cancelled.length,
      revenue,
      activeCourts: courts.filter((court) => court.status === 'ACTIVE').length,
      courts: courts.length,
      users: users.length,
    }
  }, [bookings, courts, users])

  const visibleBookings = useMemo(() => {
    const term = search.trim().toLowerCase()

    return bookings
      .filter((booking) => {
        if (statusFilter !== 'ALL' && booking.status !== statusFilter) return false
        if (courtFilter !== 'ALL' && booking.courtId !== courtFilter) return false
        if (dateFilter && toDateString(parseLocalDateTime(booking.startTime)) !== dateFilter) return false

        if (term) {
          const owner = usersById.get(booking.userId)
          const haystack = `${owner?.name ?? ''} ${owner?.email ?? ''} ${booking.id}`.toLowerCase()
          if (!haystack.includes(term)) return false
        }

        return true
      })
      .sort(
        (first, second) =>
          parseLocalDateTime(second.startTime).getTime() - parseLocalDateTime(first.startTime).getTime(),
      )
  }, [bookings, statusFilter, courtFilter, dateFilter, search, usersById])

  const bookingsByUser = useMemo(() => {
    const totals = new Map<number, { count: number; amount: number }>()

    for (const booking of bookings) {
      const current = totals.get(booking.userId) ?? { count: 0, amount: 0 }
      totals.set(booking.userId, {
        count: current.count + 1,
        amount: current.amount + (booking.status === 'CANCELLED' ? 0 : booking.courtPrice),
      })
    }

    return totals
  }, [bookings])

  function refresh() {
    setLoading(true)
    void loadData()
  }

  function reportError(caught: unknown, fallback: string) {
    setFeedback({
      tone: 'error',
      message: caught instanceof ApiError && (caught.status === 0 || caught.status === 403) ? caught.message : fallback,
    })
  }

  async function runAction(action: () => Promise<void>, successMessage: string, fallbackError: string) {
    setBusy(true)
    setFeedback(null)

    try {
      await action()
      await loadData()
      setFeedback({ tone: 'ok', message: successMessage })
    } catch (caught) {
      reportError(caught, fallbackError)
    } finally {
      setBusy(false)
      setPending(null)
    }
  }

  function handleCreateCourt(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const name = courtName.trim()
    if (!name) return

    void runAction(
      async () => {
        await createCourt({ name, type: courtType, status: 'ACTIVE' })
        setCourtName('')
      },
      `Quadra "${name}" cadastrada.`,
      'Não foi possível cadastrar a quadra. Verifique se já existe uma com esse nome.',
    )
  }

  function toggleCourtStatus(court: Court) {
    const nextStatus = court.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

    void runAction(
      async () => {
        await updateCourt(court.id, { status: nextStatus })
      },
      `Quadra "${court.name}" ${nextStatus === 'ACTIVE' ? 'ativada' : 'desativada'}.`,
      'Não foi possível atualizar o status da quadra.',
    )
  }

  function confirmPending() {
    if (!pending) return

    if (pending.type === 'cancel-booking') {
      const { booking } = pending
      void runAction(
        async () => {
          await cancelBooking(booking.id)
        },
        `Reserva AB-${String(booking.id).padStart(4, '0')} cancelada.`,
        'Não foi possível cancelar esta reserva. Ela pode já ter começado.',
      )
      return
    }

    const { court } = pending
    void runAction(
      async () => {
        await deleteCourt(court.id)
      },
      `Quadra "${court.name}" removida.`,
      'Não foi possível remover a quadra. Quadras com reservas registradas não podem ser excluídas — desative-a.',
    )
  }

  if (!allowed) {
    return (
      <div className="admin-page">
        <AppHeader
          activeItem="admin"
          navigationItems={getNavigationItems(user)}
          onNavigate={onNavigate}
          onAvatarClick={onLogout}
          avatarLabel={getInitials(user.name)}
        />

        <main className="admin-main">
          <div className="admin-denied" role="alert">
            <span>ACESSO RESTRITO</span>
            <h1>Esta área é exclusiva de administradores</h1>
            <p>
              Sua conta está cadastrada como cliente. Se você precisa gerenciar quadras e reservas, peça a um
              administrador para alterar seu perfil.
            </p>
            <button type="button" onClick={() => onNavigate('booking')}>
              Voltar para os agendamentos
            </button>
          </div>
        </main>
      </div>
    )
  }

  return (
    <div className="admin-page">
      <AppHeader
        activeItem="admin"
        navigationItems={getNavigationItems(user)}
        onNavigate={onNavigate}
        onAvatarClick={onLogout}
        avatarLabel={getInitials(user.name)}
      />

      <main className="admin-main">
        <div className="admin-heading">
          <PageHeading
            eyebrow="ÁREA RESTRITA"
            title="Painel administrativo"
            description="Acompanhe a ocupação das quadras, gerencie o cadastro e cancele reservas quando necessário."
          />
          <button type="button" className="admin-refresh" onClick={refresh} disabled={loading || busy}>
            {loading ? 'Atualizando…' : 'Atualizar dados'}
          </button>
        </div>

        {loadError && (
          <div className="admin-banner admin-banner--error" role="alert">
            {loadError}
          </div>
        )}
        {feedback && (
          <div className={`admin-banner admin-banner--${feedback.tone === 'ok' ? 'ok' : 'error'}`} role="status">
            {feedback.message}
          </div>
        )}

        <div className="admin-stats">
          <div className="admin-stat">
            <span>RESERVAS TOTAIS</span>
            <strong>{stats.total}</strong>
          </div>
          <div className="admin-stat">
            <span>AGENDADAS</span>
            <strong>{stats.scheduled}</strong>
          </div>
          <div className="admin-stat">
            <span>CANCELADAS</span>
            <strong>{stats.cancelled}</strong>
          </div>
          <div className="admin-stat">
            <span>RECEITA PREVISTA</span>
            <strong>{formatCurrency(stats.revenue)}</strong>
          </div>
          <div className="admin-stat">
            <span>QUADRAS ATIVAS</span>
            <strong>
              {stats.activeCourts}
              <small>/{stats.courts}</small>
            </strong>
          </div>
          <div className="admin-stat">
            <span>CLIENTES</span>
            <strong>{stats.users}</strong>
          </div>
        </div>

        <div className="admin-tabs" role="tablist" aria-label="Seções do painel">
          {tabs.map(({ label, value }) => (
            <button
              key={value}
              type="button"
              role="tab"
              aria-selected={value === tab}
              className={`admin-tab${value === tab ? ' admin-tab--active' : ''}`}
              onClick={() => setTab(value)}
            >
              {label}
            </button>
          ))}
        </div>

        {tab === 'bookings' && (
          <section className="admin-panel" aria-label="Reservas">
            <div className="admin-filters">
              <label>
                <span>Status</span>
                <select
                  value={statusFilter}
                  onChange={(event) => setStatusFilter(event.currentTarget.value as BookingStatus | 'ALL')}
                >
                  <option value="ALL">Todos</option>
                  <option value="SCHEDULED">Agendadas</option>
                  <option value="COMPLETED">Realizadas</option>
                  <option value="CANCELLED">Canceladas</option>
                </select>
              </label>

              <label>
                <span>Quadra</span>
                <select
                  value={String(courtFilter)}
                  onChange={(event) =>
                    setCourtFilter(event.currentTarget.value === 'ALL' ? 'ALL' : Number(event.currentTarget.value))
                  }
                >
                  <option value="ALL">Todas</option>
                  {courts.map((court) => (
                    <option key={court.id} value={court.id}>
                      {court.name}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>Data</span>
                <input type="date" value={dateFilter} onChange={(event) => setDateFilter(event.currentTarget.value)} />
              </label>

              <label className="admin-filters__search">
                <span>Cliente</span>
                <input
                  type="search"
                  placeholder="Nome, e-mail ou nº da reserva"
                  value={search}
                  onChange={(event) => setSearch(event.currentTarget.value)}
                />
              </label>

              <button
                type="button"
                className="admin-filters__reset"
                onClick={() => {
                  setStatusFilter('ALL')
                  setCourtFilter('ALL')
                  setDateFilter('')
                  setSearch('')
                }}
              >
                Limpar filtros
              </button>
            </div>

            {loading ? (
              <p className="admin-empty">Carregando reservas…</p>
            ) : (
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Código</th>
                      <th>Cliente</th>
                      <th>Quadra</th>
                      <th>Data</th>
                      <th>Horário</th>
                      <th>Duração</th>
                      <th>Valor</th>
                      <th>Status</th>
                      <th aria-label="Ações" />
                    </tr>
                  </thead>
                  <tbody>
                    {visibleBookings.map((booking) => {
                      const start = parseLocalDateTime(booking.startTime)
                      const end = parseLocalDateTime(booking.endTime)
                      const owner = usersById.get(booking.userId)
                      const court = courtsById.get(booking.courtId)
                      const canCancel = checkCancellation(booking).allowed

                      return (
                        <tr key={booking.id}>
                          <td className="admin-table__code">AB-{String(booking.id).padStart(4, '0')}</td>
                          <td>
                            <strong>{owner?.name ?? `Usuário #${booking.userId}`}</strong>
                            <small>{owner?.email ?? '—'}</small>
                          </td>
                          <td>{court?.name ?? `Quadra #${booking.courtId}`}</td>
                          <td>{formatShortDate(start)}</td>
                          <td>
                            {formatTime(start)} – {formatTime(end)}
                          </td>
                          <td>{formatDuration(getHoursBetween(start, end))}</td>
                          <td>{formatCurrency(booking.courtPrice)}</td>
                          <td>
                            <span className={`admin-status admin-status--${booking.status.toLowerCase()}`}>
                              {statusLabels[booking.status]}
                            </span>
                          </td>
                          <td className="admin-table__actions">
                            {canCancel && (
                              <button
                                type="button"
                                className="admin-action admin-action--danger"
                                onClick={() => setPending({ type: 'cancel-booking', booking })}
                                disabled={busy}
                              >
                                Cancelar
                              </button>
                            )}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>

                {visibleBookings.length === 0 && (
                  <p className="admin-empty">Nenhuma reserva encontrada com esses filtros.</p>
                )}
              </div>
            )}
          </section>
        )}

        {tab === 'courts' && (
          <section className="admin-panel" aria-label="Quadras">
            <form className="admin-court-form" onSubmit={handleCreateCourt}>
              <label>
                <span>Nome da quadra</span>
                <input
                  type="text"
                  required
                  maxLength={100}
                  placeholder="Ex.: Quadra 5 · Arena Sunset"
                  value={courtName}
                  onChange={(event) => setCourtName(event.currentTarget.value)}
                />
              </label>

              <label>
                <span>Tipo</span>
                <select value={courtType} onChange={(event) => setCourtType(event.currentTarget.value as CourtType)}>
                  <option value="OPEN">Ao ar livre — R$ 100/hora</option>
                  <option value="COVERED">Coberta — R$ 120/hora</option>
                </select>
              </label>

              <button type="submit" disabled={busy}>
                Cadastrar quadra
              </button>
            </form>

            {loading ? (
              <p className="admin-empty">Carregando quadras…</p>
            ) : (
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Nome</th>
                      <th>Tipo</th>
                      <th>Valor base</th>
                      <th>Reservas</th>
                      <th>Status</th>
                      <th aria-label="Ações" />
                    </tr>
                  </thead>
                  <tbody>
                    {courts.map((court) => {
                      const courtBookings = bookings.filter((booking) => booking.courtId === court.id).length

                      return (
                        <tr key={court.id}>
                          <td className="admin-table__code">{court.id}</td>
                          <td>
                            <strong>{court.name}</strong>
                          </td>
                          <td>{courtTypeLabels[court.type]}</td>
                          <td>
                            {formatCurrency(court.type === 'COVERED' ? 120 : 100)}
                            <small>/hora</small>
                          </td>
                          <td>{courtBookings}</td>
                          <td>
                            <span
                              className={`admin-status admin-status--${court.status === 'ACTIVE' ? 'scheduled' : 'cancelled'}`}
                            >
                              {court.status === 'ACTIVE' ? 'Ativa' : 'Inativa'}
                            </span>
                          </td>
                          <td className="admin-table__actions">
                            <button
                              type="button"
                              className="admin-action"
                              onClick={() => toggleCourtStatus(court)}
                              disabled={busy}
                            >
                              {court.status === 'ACTIVE' ? 'Desativar' : 'Ativar'}
                            </button>
                            <button
                              type="button"
                              className="admin-action admin-action--danger"
                              onClick={() => setPending({ type: 'delete-court', court })}
                              disabled={busy}
                            >
                              Excluir
                            </button>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>

                {courts.length === 0 && <p className="admin-empty">Nenhuma quadra cadastrada ainda.</p>}
              </div>
            )}
          </section>
        )}

        {tab === 'users' && (
          <section className="admin-panel" aria-label="Clientes">
            {loading ? (
              <p className="admin-empty">Carregando clientes…</p>
            ) : (
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Nome</th>
                      <th>E-mail</th>
                      <th>Reservas</th>
                      <th>Total gerado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((item) => {
                      const totals = bookingsByUser.get(item.id) ?? { count: 0, amount: 0 }

                      return (
                        <tr key={item.id}>
                          <td className="admin-table__code">{item.id}</td>
                          <td>
                            <strong>{item.name}</strong>
                          </td>
                          <td>{item.email}</td>
                          <td>{totals.count}</td>
                          <td>{formatCurrency(totals.amount)}</td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>

                {users.length === 0 && <p className="admin-empty">Nenhum cliente cadastrado ainda.</p>}
              </div>
            )}
          </section>
        )}
      </main>

      {pending && (
        <ConfirmDialog
          eyebrow={pending.type === 'cancel-booking' ? 'CANCELAR RESERVA' : 'EXCLUIR QUADRA'}
          title={
            pending.type === 'cancel-booking'
              ? (courtsById.get(pending.booking.courtId)?.name ?? `Reserva #${pending.booking.id}`)
              : pending.court.name
          }
          description={
            pending.type === 'cancel-booking' ? (
              <>
                <p>
                  Cancelar a reserva <strong>AB-{String(pending.booking.id).padStart(4, '0')}</strong> de{' '}
                  <strong>{usersById.get(pending.booking.userId)?.name ?? `usuário #${pending.booking.userId}`}</strong>
                  ?
                </p>
                <p>O sistema calcula o estorno conforme a antecedência do cancelamento.</p>
              </>
            ) : (
              <>
                <p>
                  Remover a quadra <strong>{pending.court.name}</strong> do catálogo?
                </p>
                <p>Quadras que já possuem reservas não podem ser excluídas — nesse caso, desative-a.</p>
              </>
            )
          }
          confirmLabel={busy ? 'Processando…' : 'Confirmar'}
          dismissLabel="Voltar"
          onConfirm={confirmPending}
          onClose={() => setPending(null)}
        />
      )}
    </div>
  )
}
