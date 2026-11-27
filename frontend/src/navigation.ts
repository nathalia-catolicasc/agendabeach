import type { NavigationItem } from './components'
import type { SessionUser } from './session'
import { isAdmin } from './session'

const baseItems: NavigationItem[] = [
  { label: 'Agendar', value: 'booking' },
  { label: 'Minhas reservas', value: 'reservations' },
  { label: 'Cancelar reserva', value: 'cancel' },
]

/** O item de administração só aparece para quem o backend reconhece como ADMIN. */
export function getNavigationItems(user: SessionUser | null): NavigationItem[] {
  return isAdmin(user) ? [...baseItems, { label: 'Administração', value: 'admin' }] : baseItems
}
