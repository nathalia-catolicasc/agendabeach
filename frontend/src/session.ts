import type { LoginResponse } from './api/types'

export type SessionUser = LoginResponse

const STORAGE_KEY = 'agendabeach.session'
export const SESSION_CLEARED_EVENT = 'agendabeach:session-cleared'
let memorySession: SessionUser | null = null

function isSessionUser(value: unknown): value is SessionUser {
  if (!value || typeof value !== 'object') return false
  const user = value as Partial<SessionUser>
  return (
    typeof user.id === 'number' &&
    typeof user.name === 'string' &&
    typeof user.email === 'string' &&
    (user.profile === 'CLIENT' || user.profile === 'ADMIN') &&
    typeof user.accessToken === 'string' &&
    user.accessToken.length > 0 &&
    user.tokenType === 'Bearer' &&
    typeof user.expiresAt === 'string'
  )
}

export function getStoredSession(): SessionUser | null {
  if (memorySession) return memorySession
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed: unknown = JSON.parse(raw)
    if (!isSessionUser(parsed)) {
      window.localStorage.removeItem(STORAGE_KEY)
      return null
    }
    memorySession = parsed
    return parsed
  } catch {
    return null
  }
}

export function storeSession(user: SessionUser) {
  memorySession = user
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  } catch {
    /* modo privado / storage bloqueado: a sessão vive só em memória */
  }
}

export function clearStoredSession() {
  memorySession = null
  try {
    window.localStorage.removeItem(STORAGE_KEY)
  } catch {
    /* nada a fazer */
  }
  window.dispatchEvent(new Event(SESSION_CLEARED_EVENT))
}

export function isAdmin(user: SessionUser | null) {
  return user?.profile === 'ADMIN'
}

export function canTryAdmin(user: SessionUser | null) {
  return isAdmin(user)
}

export function getInitials(name: string) {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return 'AB'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}
