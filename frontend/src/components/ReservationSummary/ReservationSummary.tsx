import './ReservationSummary.css'

type ReservationSummaryProps = {
  selectedDate: Date | null
  onOpenBooking: () => void
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

function getFullDateLabel(date: Date) {
  return new Intl.DateTimeFormat('pt-BR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date)
}

export function ReservationSummary({ selectedDate, onOpenBooking }: ReservationSummaryProps) {
  return (
    <aside className="booking-summary" aria-live="polite">
      <div className="booking-summary__header">
        <h2>Resumo da reserva</h2>
        <p>{selectedDate ? 'Continue preenchendo as etapas' : 'Preencha as etapas ao lado'}</p>
      </div>

      <div className="booking-summary__body">
        <div className="booking-summary__icon"><CalendarIcon /></div>
        {selectedDate ? (
          <>
            <span className="booking-summary__label">DATA SELECIONADA</span>
            <strong>{getFullDateLabel(selectedDate)}</strong>
            <p>Agora escolha a quadra e o melhor horário para sua partida.</p>
            <button type="button" onClick={onOpenBooking}>Escolher quadra e horário</button>
          </>
        ) : (
          <p>Nenhum dia selecionado.<br />Comece escolhendo uma data no calendário.</p>
        )}
      </div>
    </aside>
  )
}
