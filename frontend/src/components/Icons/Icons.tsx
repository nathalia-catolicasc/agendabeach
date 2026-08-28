export type IconProps = {
  size?: number
  stroke?: number
}

export function MailIcon({ size = 18, stroke = 1.8 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 6.5h16v11H4z" stroke="currentColor" strokeWidth={stroke} strokeLinejoin="round" />
      <path d="m5 8 7 5 7-5" stroke="currentColor" strokeWidth={stroke} strokeLinejoin="round" />
    </svg>
  )
}

export function UserIcon({ size = 18, stroke = 1.8 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="8" r="3.5" stroke="currentColor" strokeWidth={stroke} />
      <path d="M5.5 20c.7-4 2.8-6 6.5-6s5.8 2 6.5 6" stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" />
    </svg>
  )
}

export function LockIcon({ size = 18, stroke = 1.8 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="5" y="10" width="14" height="10" rx="2" stroke="currentColor" strokeWidth={stroke} />
      <path d="M8.5 10V7.5a3.5 3.5 0 0 1 7 0V10" stroke="currentColor" strokeWidth={stroke} />
    </svg>
  )
}

export function ArrowIcon({ size = 18, stroke = 2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

export function CheckIcon({ size = 16, stroke = 2.2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="m5 12 4 4L19 6" stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}
