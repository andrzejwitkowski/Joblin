import { useEffect, useId, useRef } from 'react'
import type { JobOffer } from './api'

import { STATUS_META } from './offerStatus'

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
      className="fixed inset-0 z-40 m-0 h-full max-h-none w-full max-w-none bg-transparent p-0 open:flex open:justify-end"
      aria-labelledby={titleId}
      onClose={onClose}
      onClick={(e) => {
        if (e.target === dialogRef.current) onClose()
      }}
    >
      <aside
        className="h-full w-full max-w-md overflow-y-auto border-l border-slate-200 bg-white p-5 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          className="mb-4 text-sm text-slate-500 hover:text-slate-800"
          onClick={onClose}
          autoFocus
        >
          Zamknij
        </button>
        <h2 id={titleId} className="text-2xl font-semibold text-slate-900">
          {offer.title}
        </h2>
        <p className="mt-1 text-slate-500">{offer.company}</p>
        {offer.salary && <p className="mt-3 text-sm font-semibold text-slate-800">{offer.salary}</p>}
        <a
          className="mt-3 inline-block text-sm font-medium text-[var(--brand)] underline hover:text-[var(--brand-600)]"
          href={offer.sourceUrl}
          target="_blank"
          rel="noreferrer"
        >
          Otwórz źródło
        </a>
        <p className="mt-4 text-sm leading-relaxed whitespace-pre-wrap text-slate-700">{offer.description}</p>
        {offer.tags.length > 0 && (
          <div className="mt-4 flex flex-wrap gap-2">
            {offer.tags.map((t) => (
              <span
                key={t}
                className="rounded border border-slate-200 bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700"
              >
                {t}
              </span>
            ))}
          </div>
        )}
        <dl className="mt-6 space-y-1 text-xs text-slate-500">
          <div>Status oferty: {STATUS_META[offer.status].label}</div>
          <div>Źródło: {offer.sourceBot}</div>
          <div>Znaleziono: {new Date(offer.foundAt).toLocaleString('pl-PL')}</div>
          <div>Aktualizacja: {new Date(offer.updatedAt).toLocaleString('pl-PL')}</div>
        </dl>
      </aside>
    </dialog>
  )
}
