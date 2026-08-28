import { Logo } from '../Logo'
import './AppHeader.css'

export type NavigationItem = {
  label: string
  value: string
}

type AppHeaderProps = {
  activeItem: string
  navigationItems: NavigationItem[]
  onNavigate?: (value: string) => void
  onAvatarClick?: () => void
  avatarLabel?: string
}

export function AppHeader({
  activeItem,
  navigationItems,
  onNavigate,
  onAvatarClick,
  avatarLabel = 'AB',
}: AppHeaderProps) {
  return (
    <header className="app-header">
      <Logo compact />

      <nav className="app-header__nav" aria-label="Navegação principal">
        {navigationItems.map((item) => (
          <button
            key={item.value}
            type="button"
            className={item.value === activeItem ? 'app-header__nav-item--active' : undefined}
            onClick={() => onNavigate?.(item.value)}
            aria-current={item.value === activeItem ? 'page' : undefined}
          >
            {item.label}
          </button>
        ))}
      </nav>

      <button
        type="button"
        className="app-header__avatar"
        onClick={onAvatarClick}
        title="Sair da conta"
        aria-label="Sair da conta"
      >
        {avatarLabel}
      </button>
    </header>
  )
}
