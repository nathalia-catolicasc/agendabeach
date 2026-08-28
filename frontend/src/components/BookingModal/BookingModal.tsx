import { useEffect, useMemo, useState } from 'react'
import './BookingModal.css'

type Court = {
  id: string
  name: string
  surface: string
  pricePerHour: number
  tags: string[]
  unavailableSlots: string[]
}

type Equipment = {
  id: string
  name: string
  description: string
  price: number
  max: number
}

type Duration = {
  label: string
  hours: number
}

export type ReservationDetails = {
  date: Date
  court: Court
  startTime: string
  endTime: string
  hours: number
  equipments: { equipment: Equipment; quantity: number }[]
  total: number
}

type BookingModalProps = {
  selectedDate: Date
  onClose: () => void
  onConfirm?: (reservation: ReservationDetails) => void
}

const courts: Court[] = [
  {
    id: 'arena-sunset',
    name: 'Quadra 1 · Arena Sunset',
    surface: 'Areia oficial · Coberta',
    pricePerHour: 120,
    tags: ['Beach-Tennis', 'Iluminação LED'],
    unavailableSlots: ['09:00', '10:00', '19:00'],
  },
  {
    id: 'beira-mar',
    name: 'Quadra 2 · Beira Mar',
    surface: 'Areia fina · Ao ar livre',
    pricePerHour: 100,
    tags: ['Beach-Tennis', 'Vista para o mar'],
    unavailableSlots: ['08:00', '17:00', '18:00', '21:00'],
  },
  {
    id: 'Beach-Tennis',
    name: 'Quadra 3 · Beach Tennis Center',
    surface: 'Areia compactada · Coberta',
    pricePerHour: 110,
    tags: ['Beach tennis', 'Arquibancada'],
    unavailableSlots: ['07:00', '12:00', '20:00'],
  },
  {
    id: 'volei-pro',
    name: 'Quadra 4 · Vôlei Pro',
    surface: 'Areia oficial · Ao ar livre',
    pricePerHour: 140,
    tags: ['Beach-Tennis', 'Placar eletrônico'],
    unavailableSlots: ['11:00', '15:00', '16:00'],
  },
]

const equipments: Equipment[] = [
  { id: 'bola', name: 'Bola oficial', description: 'Bola de vôlei ou futevôlei', price: 15, max: 4 },
  { id: 'raquetes', name: 'Par de raquetes', description: 'Beach tennis com bolinhas', price: 25, max: 4 },
  { id: 'rede', name: 'Rede extra', description: 'Rede reserva com regulagem de altura', price: 30, max: 2 },
  { id: 'iluminacao', name: 'Iluminação noturna', description: 'Refletores para jogos após as 18h', price: 40, max: 1 },
  { id: 'coletes', name: 'Kit de coletes', description: '10 coletes numerados', price: 10, max: 3 },
  { id: 'cooler', name: 'Cooler com gelo', description: 'Cooler 20L com água gelada', price: 20, max: 2 },
]

const durations: Duration[] = [
  { label: '1 hora', hours: 1 },
  { label: '1h30', hours: 1.5 },
  { label: '2 horas', hours: 2 },
]

const openingHour = 7
const closingHour = 23

const timeSlots = Array.from(
  { length: closingHour - openingHour },
  (_, index) => `${String(openingHour + index).padStart(2, '0')}:00`,
)

const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function formatPrice(value: number) {
  return currencyFormatter.format(value)
}

function getFullDateLabel(date: Date) {
  return new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date)
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

function getDurationLabel(hours: number) {
  return durations.find((duration) => duration.hours === hours)?.label ?? ''
}

function CloseIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="m7 7 10 10M17 7 7 17" />
    </svg>
  )
}

function ClockIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </svg>
  )
}

function MinusIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M6 12h12" />
    </svg>
  )
}

function PlusIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M12 6v12M6 12h12" />
    </svg>
  )
}

export function BookingModal({ selectedDate, onClose, onConfirm }: BookingModalProps) {
  const [selectedCourtId, setSelectedCourtId] = useState<string | null>(null)
  const [selectedTime, setSelectedTime] = useState<string | null>(null)
  const [selectedHours, setSelectedHours] = useState(1)
  const [quantities, setQuantities] = useState<Record<string, number>>({})

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [onClose])

  const selectedCourt = courts.find((court) => court.id === selectedCourtId) ?? null

  const courtSlots = useMemo(() => {
    if (!selectedCourt) return []

    return timeSlots.map((slot) => {
      const startMinutes = toMinutes(slot)
      const endMinutes = startMinutes + selectedHours * 60
      const overlapsBooked = selectedCourt.unavailableSlots.some((booked) => {
        const bookedStart = toMinutes(booked)
        return bookedStart >= startMinutes && bookedStart < endMinutes
      })

      return { slot, disabled: overlapsBooked || endMinutes > closingHour * 60 }
    })
  }, [selectedCourt, selectedHours])

  const chosenEquipments = useMemo(
    () => equipments
      .map((equipment) => ({ equipment, quantity: quantities[equipment.id] ?? 0 }))
      .filter(({ quantity }) => quantity > 0),
    [quantities],
  )

  const courtTotal = selectedCourt ? selectedCourt.pricePerHour * selectedHours : 0
  const equipmentTotal = chosenEquipments.reduce(
    (total, { equipment, quantity }) => total + equipment.price * quantity,
    0,
  )
  const serviceFee = (courtTotal + equipmentTotal) * 0.05
  const total = courtTotal + equipmentTotal + serviceFee
  const endTime = selectedTime ? toTimeLabel(toMinutes(selectedTime) + selectedHours * 60) : null
  const isComplete = Boolean(selectedCourt && selectedTime)

  function changeQuantity(equipmentId: string, offset: number, max: number) {
    setQuantities((current) => ({
      ...current,
      [equipmentId]: Math.min(Math.max((current[equipmentId] ?? 0) + offset, 0), max),
    }))
  }

  function selectCourt(courtId: string) {
    setSelectedCourtId(courtId)
    setSelectedTime(null)
  }

  function changeDuration(hours: number) {
    setSelectedHours(hours)
    setSelectedTime(null)
  }

  function confirmReservation() {
    if (!selectedCourt || !selectedTime || !endTime) return

    onConfirm?.({
      date: selectedDate,
      court: selectedCourt,
      startTime: selectedTime,
      endTime,
      hours: selectedHours,
      equipments: chosenEquipments,
      total,
    })
  }

  return (
    <div className="booking-modal" role="dialog" aria-modal="true" aria-labelledby="booking-modal-title">
      <button type="button" className="booking-modal__backdrop" onClick={onClose} tabIndex={-1} aria-hidden="true" />

      <div className="booking-modal__dialog">
        <header className="booking-modal__header">
          <div>
            <span>RESERVA DE QUADRA</span>
            <h2 id="booking-modal-title">{getFullDateLabel(selectedDate)}</h2>
          </div>
          <button type="button" onClick={onClose} aria-label="Fechar reserva"><CloseIcon /></button>
        </header>

        <div className="booking-modal__content">
          <div className="booking-modal__steps">
            <section className="booking-modal__section" aria-labelledby="booking-step-court">
              <div className="booking-step">
                <span>2</span>
                <strong id="booking-step-court">ESCOLHA A QUADRA</strong>
              </div>

              <div className="court-list">
                {courts.map((court) => (
                  <button
                    key={court.id}
                    type="button"
                    className={`court-option${court.id === selectedCourtId ? ' court-option--selected' : ''}`}
                    onClick={() => selectCourt(court.id)}
                    aria-pressed={court.id === selectedCourtId}
                  >
                    <strong>{court.name}</strong>
                    <span className="court-option__surface">{court.surface}</span>
                    <span className="court-option__tags">
                      {court.tags.map((tag) => <i key={tag}>{tag}</i>)}
                    </span>
                    <span className="court-option__price">
                      {formatPrice(court.pricePerHour)}<small>/hora</small>
                    </span>
                  </button>
                ))}
              </div>
            </section>

            <section className="booking-modal__section" aria-labelledby="booking-step-time">
              <div className="booking-step">
                <span>3</span>
                <strong id="booking-step-time">HORÁRIO E DURAÇÃO</strong>
              </div>

              <div className="duration-list" role="group" aria-label="Duração da reserva">
                {durations.map(({ label, hours }) => (
                  <button
                    key={label}
                    type="button"
                    className={`duration-option${hours === selectedHours ? ' duration-option--selected' : ''}`}
                    onClick={() => changeDuration(hours)}
                    aria-pressed={hours === selectedHours}
                  >
                    <ClockIcon />
                    {label}
                  </button>
                ))}
              </div>

              {selectedCourt ? (
                <div className="time-grid">
                  {courtSlots.map(({ slot, disabled }) => (
                    <button
                      key={slot}
                      type="button"
                      className={`time-slot${slot === selectedTime ? ' time-slot--selected' : ''}`}
                      disabled={disabled}
                      onClick={() => setSelectedTime(slot)}
                      aria-pressed={slot === selectedTime}
                    >
                      {slot}
                    </button>
                  ))}
                </div>
              ) : (
                <p className="booking-modal__hint">Escolha uma quadra para ver os horários disponíveis.</p>
              )}
            </section>

            <section className="booking-modal__section" aria-labelledby="booking-step-equipment">
              <div className="booking-step">
                <span>4</span>
                <strong id="booking-step-equipment">EQUIPAMENTOS (OPCIONAL)</strong>
              </div>

              <ul className="equipment-list">
                {equipments.map((equipment) => {
                  const quantity = quantities[equipment.id] ?? 0

                  return (
                    <li
                      key={equipment.id}
                      className={`equipment-item${quantity > 0 ? ' equipment-item--selected' : ''}`}
                    >
                      <div className="equipment-item__info">
                        <strong>{equipment.name}</strong>
                        <span>{equipment.description}</span>
                      </div>

                      <span className="equipment-item__price">{formatPrice(equipment.price)}</span>

                      <div className="equipment-item__stepper">
                        <button
                          type="button"
                          onClick={() => changeQuantity(equipment.id, -1, equipment.max)}
                          disabled={quantity === 0}
                          aria-label={`Remover ${equipment.name}`}
                        >
                          <MinusIcon />
                        </button>
                        <output aria-label={`Quantidade de ${equipment.name}`}>{quantity}</output>
                        <button
                          type="button"
                          onClick={() => changeQuantity(equipment.id, 1, equipment.max)}
                          disabled={quantity >= equipment.max}
                          aria-label={`Adicionar ${equipment.name}`}
                        >
                          <PlusIcon />
                        </button>
                      </div>
                    </li>
                  )
                })}
              </ul>
            </section>
          </div>

          <aside className="booking-modal__details" aria-live="polite">
            <h3>Detalhes da reserva</h3>

            <dl className="reservation-details">
              <div>
                <dt>Data</dt>
                <dd>{getFullDateLabel(selectedDate)}</dd>
              </div>
              <div>
                <dt>Quadra</dt>
                <dd>{selectedCourt ? selectedCourt.name : <em>Não selecionada</em>}</dd>
              </div>
              <div>
                <dt>Horário</dt>
                <dd>{selectedTime && endTime ? `${selectedTime} às ${endTime}` : <em>Não selecionado</em>}</dd>
              </div>
              <div>
                <dt>Duração</dt>
                <dd>{getDurationLabel(selectedHours)}</dd>
              </div>
              <div>
                <dt>Equipamentos</dt>
                <dd>
                  {chosenEquipments.length === 0
                    ? <em>Nenhum</em>
                    : chosenEquipments.map(({ equipment, quantity }) => (
                      <span key={equipment.id}>{quantity}× {equipment.name}</span>
                    ))}
                </dd>
              </div>
            </dl>

            <ul className="reservation-costs">
              <li>
                <span>Quadra ({getDurationLabel(selectedHours)})</span>
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
            </ul>

            <div className="reservation-total">
              <span>Total</span>
              <strong>{formatPrice(total)}</strong>
            </div>

            <button type="button" className="reservation-confirm" disabled={!isComplete} onClick={confirmReservation}>
              {isComplete ? 'Confirmar reserva' : 'Escolha quadra e horário'}
            </button>

            <p className="reservation-note">
              Pagamento na chegada. Cancelamento gratuito até 12 horas antes do início da partida.
            </p>
          </aside>
        </div>
      </div>
    </div>
  )
}
