import { useState } from 'react'
import { AppHeader, Calendar, PageHeading, ReservationSummary } from './components'
import './BookingPage.css'

const navigationItems = [
  { label: 'Início', value: 'home' },
  { label: 'Agendar', value: 'booking' },
  { label: 'Minhas reservas', value: 'reservations' },
  { label: 'Quadras', value: 'courts' },
]

type BookingPageProps = {
  onLogout: () => void
}

export default function BookingPage({ onLogout }: BookingPageProps) {
  const [displayedMonth, setDisplayedMonth] = useState(new Date(2026, 5, 1))
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)

  function changeMonth(offset: number) {
    setDisplayedMonth((currentMonth) => new Date(currentMonth.getFullYear(), currentMonth.getMonth() + offset, 1))
  }

  return (
    <div className="booking-page">
      <AppHeader
        activeItem="booking"
        navigationItems={navigationItems}
        onAvatarClick={onLogout}
      />

      <main className="booking-main">
        <PageHeading
          eyebrow="RESERVAS ONLINE"
          title="Agendar quadra"
          description="Toque em um dia disponível no calendário para escolher a quadra e o horário."
        />

        <div className="booking-layout">
          <Calendar
            displayedMonth={displayedMonth}
            selectedDate={selectedDate}
            onChangeMonth={changeMonth}
            onSelectDate={setSelectedDate}
          />
          <ReservationSummary selectedDate={selectedDate} />
        </div>
      </main>
    </div>
  )
}
