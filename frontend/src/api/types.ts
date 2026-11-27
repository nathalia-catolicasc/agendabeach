export type CourtType = 'COVERED' | 'OPEN'
export type CourtStatus = 'ACTIVE' | 'INACTIVE'
export type BookingStatus = 'SCHEDULED' | 'CANCELLED' | 'COMPLETED'
export type ProfileType = 'CLIENT' | 'ADMIN'

/** Resposta de /courts (CourtResponseDTO). */
export type Court = {
  id: number
  name: string
  type: CourtType
  status: CourtStatus
}

/** Resposta de /bookings (BookingResponseDTO). Datas chegam como ISO local, sem fuso. */
export type Booking = {
  id: number
  userId: number
  courtId: number
  startTime: string
  endTime: string
  courtPrice: number
  status: BookingStatus
  /** Política de cancelamento vigente (RN04); null quando a reserva não pode mais ser cancelada. */
  cancellationPolicy?: CancellationPolicy | null
}

export type CancellationType = 'REFUND' | 'CREDIT' | 'NONE'

/** Calculada pelo backend (CancellationPolicyDTO) — o frontend exibe, não recalcula. */
export type CancellationPolicy = {
  type: CancellationType
  percentage: number
  refundAmount: number
  creditAmount: number
  refundDeadline: string
  creditDeadline: string
}

/** Resposta de /users (UserResponseDTO). */
export type ApiUser = {
  id: number
  name: string
  email: string
  profile: ProfileType
}

export type LoginResponse = ApiUser & {
  accessToken: string
  tokenType: 'Bearer'
  expiresAt: string
}

/** Resposta de POST /bookings/{id}/cancel (CancellationResponseDTO). */
export type Cancellation = {
  bookingId: number
  refundAmount: number
  status: string
  cancelledAt: string
}

export type CreateBookingPayload = {
  userId: number
  courtId: number
  startTime: string
  endTime: string
}

export type CourtPayload = {
  name?: string
  type?: CourtType
  status?: CourtStatus
}
