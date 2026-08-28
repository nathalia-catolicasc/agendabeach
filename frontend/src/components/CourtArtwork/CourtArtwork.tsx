import './CourtArtwork.css'

export function CourtArtwork() {
  return (
    <div className="court-art" aria-hidden="true">
      <span className="court-art__ball court-art__ball--one" />
      <span className="court-art__ball court-art__ball--two" />
      <div className="court-art__glow" />
      <svg viewBox="0 0 510 390" className="court-art__lines">
        <path d="M96 319 182 92h165l78 227H96Z" fill="rgba(8,53,88,.43)" stroke="rgba(255,255,255,.24)" strokeWidth="2" />
        <path d="m181 92 48 227m118-227-57 227M138 211h247" stroke="rgba(255,255,255,.32)" strokeWidth="2" />
        <path d="M119 263h286" stroke="#E8B441" strokeWidth="4" strokeLinecap="round" />
        <path d="M128 265v-47m268 47v-47" stroke="#E8B441" strokeWidth="5" strokeLinecap="round" />
        <path d="M128 228c90 9 179 9 268 0" stroke="#F2D480" strokeWidth="2" strokeDasharray="5 5" />
        <g transform="translate(322 57) rotate(24)">
          <ellipse cx="34" cy="49" rx="28" ry="39" fill="rgba(232,180,65,.13)" stroke="#E8B441" strokeWidth="7" />
          <path d="M18 21 50 75M8 40l44 20M10 59l40-18M22 11l33 49M17 86l-13 38" stroke="#E8B441" strokeWidth="3" strokeLinecap="round" />
          <path d="M8 122c7 5 11 5 18 0" stroke="#F2D480" strokeWidth="5" strokeLinecap="round" />
        </g>
      </svg>
      <div className="court-art__badge">
        <span>✓</span>
        <div><strong>Reserva confirmada</strong><small>Sua quadra está garantida</small></div>
      </div>
    </div>
  )
}
