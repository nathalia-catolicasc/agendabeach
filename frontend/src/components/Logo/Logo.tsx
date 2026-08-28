import { useId } from 'react'
import './Logo.css'

type LogoProps = {
  compact?: boolean
  showTagline?: boolean
}

export function Logo({ compact = false, showTagline = false }: LogoProps) {
  const gradientId = useId().replaceAll(':', '')

  return (
    <div className={`app-brand${compact ? ' app-brand--compact' : ''}`} aria-label="Agenda Beach">
      <svg className="app-brand__mark" viewBox="0 0 68 68" role="img" aria-hidden="true">
        <defs>
          <linearGradient id={gradientId} x1="8" y1="4" x2="58" y2="64" gradientUnits="userSpaceOnUse">
            <stop stopColor="#f6cf68" />
            <stop offset="1" stopColor="#dfa72c" />
          </linearGradient>
        </defs>
        <rect x="6" y="9" width="56" height="54" rx="15" fill="#0b426c" />
        <path d="M6 23c0-7.7 6.3-14 14-14h28c7.7 0 14 6.3 14 14v3H6v-3Z" fill={`url(#${gradientId})`} />
        <ellipse cx="34" cy="40" rx="10" ry="13" transform="rotate(-18 34 40)" fill="none" stroke="#fff9ec" strokeWidth="2.4" />
        <path d="m37.5 52 3.8 8" stroke="#fff9ec" strokeWidth="2.4" strokeLinecap="round" />
        <circle cx="31" cy="35" r="1.3" fill="#fff9ec" />
        <circle cx="36" cy="39" r="1.3" fill="#fff9ec" />
        <circle cx="32" cy="44" r="1.3" fill="#fff9ec" />
      </svg>

      <div className="app-brand__wordmark">
        <div><strong>Agenda</strong><span>Beach</span></div>
        {showTagline && <small>AGENDAMENTO DE QUADRAS</small>}
      </div>
    </div>
  )
}
