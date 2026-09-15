import { useEffect, useId, useRef } from 'react'
import type { JobOffer } from './api'

export function OfferDrawer({ offer, onClose }: { offer: JobOffer; onClose: () => void }) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const titleId = useId()
  const previouslyFocused = useRef<HTMLElement | null>(null)

  useEffect(() => {
    previouslyFocused.current = document.activeElement as HTMLElement | null
    const dialog = dialogRef.current
    if (!dialog) return
    if (!dialog.open) dialog.showModal()
    return () => {
      if (dialog.open) dialog.close()
      previouslyFocused.current?.focus()
    }
  }, [])

  return (
    <dialog
      ref={dialogRef}
      className="fixed inset-0 z-20 m-0 h-full max-h-none w-full max-w-none bg-transparent p-0 open:flex open:justify-end"
      aria-labelledby={titleId}
      onClose={onClose}
      onClick={(e) => {
        if (e.target === dialogRef.current) onClose()
      }}
    >
      <aside
        className="h-full w-full max-w-md overflow-y-auto border-l border-[var(--line)] bg-[var(--paper)] p-5 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <button type="button" className="mb-4 text-sm text-[var(--muted)]" onClick={onClose} autoFocus>
          Close
        </button>
        <h2 id={titleId} className="text-2xl font-semibold">
          {offer.title}
        </h2>
        <p className="mt-1 text-[var(--muted)]">{offer.company}</p>
        {offer.salary && <p className="mt-3 text-sm">Salary: {offer.salary}</p>}
        <a
          className="mt-3 inline-block text-sm text-[var(--accent)] underline"
          href={offer.sourceUrl}
          target="_blank"
          rel="noreferrer"
        >
          Open source
        </a>
        <p className="mt-4 whitespace-pre-wrap text-sm leading-relaxed">{offer.description}</p>
        {offer.tags.length > 0 && (
          <div className="mt-4 flex flex-wrap gap-2">
            {offer.tags.map((t) => (
              <span key={t} className="rounded bg-[var(--panel)] px-2 py-0.5 text-xs">
                {t}
              </span>
            ))}
          </div>
        )}
        <dl className="mt-6 space-y-1 text-xs text-[var(--muted)]">
          <div>Status: {offer.status}</div>
          <div>Source: {offer.sourceBot}</div>
          <div>Found: {new Date(offer.foundAt).toLocaleString()}</div>
          <div>Updated: {new Date(offer.updatedAt).toLocaleString()}</div>
        </dl>
      </aside>
    </dialog>
  )
}
