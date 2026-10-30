import { ApiError, request } from './http'
import type { ApiUser, Booking, Cancellation, Court, CourtPayload, CreateBookingPayload, LoginResponse } from './types'

export { ApiError }
export type * from './types'

/* ------------------------------------------------------------------ usuários */

export function login(email: string, password: string) {
  return request<LoginResponse>('/users/login', { method: 'POST', body: { email, password }, auth: false })
}

export function register(name: string, email: string, password: string, cpf: string) {
  return request<ApiUser>('/users', { method: 'POST', body: { name, email, password, cpf }, auth: false })
}

export function logout() {
  return request<void>('/users/logout', { method: 'POST' })
}

export function listUsers() {
  return request<ApiUser[]>('/users')
}

export function listUserBookings(userId: number) {
  return request<Booking[]>(`/users/${userId}/bookings`)
}

/* ------------------------------------------------------------------- quadras */

export function listCourts() {
  return request<Court[]>('/courts')
}

export function createCourt(payload: CourtPayload) {
  return request<Court>('/courts', { method: 'POST', body: payload })
}

export function updateCourt(id: number, payload: CourtPayload) {
  return request<Court>(`/courts/${id}`, { method: 'PUT', body: payload })
}

export function deleteCourt(id: number) {
  return request<void>(`/courts/${id}`, { method: 'DELETE' })
}

/* ------------------------------------------------------------------ reservas */

export function listBookings() {
  return request<Booking[]>('/bookings')
}

export function searchBookings(date: string, courtId?: number) {
  const query = new URLSearchParams({ date })
  if (courtId !== undefined) query.set('courtId', String(courtId))
  return request<Booking[]>(`/bookings/search?${query.toString()}`)
}

export function createBooking(payload: CreateBookingPayload) {
  return request<Booking>('/bookings', { method: 'POST', body: payload })
}

export function cancelBooking(id: number) {
  return request<Cancellation>(`/bookings/${id}/cancel`, { method: 'POST' })
}
