import { useEffect, useId, useRef } from 'react'
import type { JobOffer } from './api'
import { OfferSectionsPreview } from './offerSections'

export function OfferDrawer({
  offer,
  onClose,
  onOpenFull,
}: {
  offer: JobOffer
  onClose: () => void
  onOpenFull: () => void
}) {
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
        {(offer.salary || offer.location) && (
          <p className="mt-2 text-sm text-[var(--muted)]">
            {[offer.salary, offer.location].filter(Boolean).join(' · ')}
          </p>
        )}
        <div className="mt-3 flex flex-wrap gap-3">
          <a
            className="inline-block text-sm text-[var(--accent)] underline"
            href={offer.sourceUrl}
            target="_blank"
            rel="noreferrer"
          >
            Open source
          </a>
          <button type="button" className="text-sm font-medium text-[var(--accent)] underline" onClick={onOpenFull}>
            Full view
          </button>
        </div>
        <OfferSectionsPreview offer={offer} />
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
