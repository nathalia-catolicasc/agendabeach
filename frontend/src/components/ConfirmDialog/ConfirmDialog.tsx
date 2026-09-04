import { useEffect } from 'react'
import type { ReactNode } from 'react'
import './ConfirmDialog.css'

type ConfirmDialogProps = {
  eyebrow: string
  title: string
  description: ReactNode
  confirmLabel: string
  dismissLabel?: string
  onConfirm: () => void
  onClose: () => void
}

function CloseIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="m7 7 10 10M17 7 7 17" />
    </svg>
  )
}

export function ConfirmDialog({
  eyebrow,
  title,
  description,
  confirmLabel,
  dismissLabel = 'Voltar',
  onConfirm,
  onClose,
}: ConfirmDialogProps) {
  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [onClose])

  return (
    <div className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-dialog-title">
      <button type="button" className="confirm-dialog__backdrop" onClick={onClose} tabIndex={-1} aria-hidden="true" />

      <div className="confirm-dialog__panel">
        <header className="confirm-dialog__header">
          <div>
            <span>{eyebrow}</span>
            <h2 id="confirm-dialog-title">{title}</h2>
          </div>
          <button type="button" onClick={onClose} aria-label="Fechar"><CloseIcon /></button>
        </header>

        <div className="confirm-dialog__body">{description}</div>

        <footer className="confirm-dialog__footer">
          <button type="button" className="confirm-dialog__dismiss" onClick={onClose}>{dismissLabel}</button>
          <button type="button" className="confirm-dialog__confirm" onClick={onConfirm}>{confirmLabel}</button>
        </footer>
      </div>
    </div>
  )
}
