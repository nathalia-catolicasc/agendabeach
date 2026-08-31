import { useState } from 'react'
import { AppHeader, BookingModal, Calendar, PageHeading, ReservationSummary } from './components'
import type { ReservationDetails } from './components'
import { navigationItems } from './navigation'
import './BookingPage.css'

type BookingPageProps = {
  onNavigate: (value: string) => void
  onLogout: () => void
}

export default function BookingPage({ onNavigate, onLogout }: BookingPageProps) {
  const [displayedMonth, setDisplayedMonth] = useState(new Date(2026, 5, 1))
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)
  const [isBookingModalOpen, setIsBookingModalOpen] = useState(false)

  function changeMonth(offset: number) {
    setDisplayedMonth((currentMonth) => new Date(currentMonth.getFullYear(), currentMonth.getMonth() + offset, 1))
  }

  function confirmReservation(reservation: ReservationDetails) {
    console.log('Reserva confirmada', reservation)
    setIsBookingModalOpen(false)
  }

  return (
    <div className="booking-page">
      <AppHeader
        activeItem="booking"
        navigationItems={navigationItems}
        onNavigate={onNavigate}
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
          <ReservationSummary selectedDate={selectedDate} onOpenBooking={() => setIsBookingModalOpen(true)} />
        </div>
      </main>

      {isBookingModalOpen && selectedDate && (
        <BookingModal
          selectedDate={selectedDate}
          onClose={() => setIsBookingModalOpen(false)}
          onConfirm={confirmReservation}
        />
      )}
    </div>
  )
}
