import { useEffect, useId, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import type { JobOffer } from './api'
import { formatDateTime } from './i18n/format'
import { statusLabel } from './offerStatus'
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
  const { t } = useTranslation()
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
        className="h-full w-full max-w-md overflow-y-auto border-l border-border bg-surface p-5 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          className="mb-4 text-sm text-ink-muted hover:text-ink"
          onClick={onClose}
          autoFocus
        >
          {t('common.close')}
        </button>
        <h2 id={titleId} className="text-2xl font-semibold text-ink">
          {offer.title}
        </h2>
        <p className="mt-1 text-ink-muted">{offer.company}</p>
        {(offer.salary || offer.location) && (
          <p className="mt-3 text-sm font-semibold text-ink">
            {[offer.salary, offer.location].filter(Boolean).join(' · ')}
          </p>
        )}
        <div className="mt-3 flex flex-wrap gap-3">
          <a
            className="inline-block text-sm font-medium text-brand underline hover:text-brand-600"
            href={offer.sourceUrl}
            target="_blank"
            rel="noreferrer"
          >
            {t('drawer.openSource')}
          </a>
          <button
            type="button"
            className="text-sm font-medium text-brand underline hover:text-brand-600"
            onClick={onOpenFull}
          >
            {t('drawer.fullView')}
          </button>
        </div>
        <OfferSectionsPreview offer={offer} />
        <dl className="mt-6 space-y-1 text-xs text-ink-muted">
          <div>{t('drawer.offerStatus', { status: statusLabel(offer.status, t) })}</div>
          <div>{t('drawer.source', { bot: offer.sourceBot })}</div>
          <div>{t('drawer.foundAt', { when: formatDateTime(offer.foundAt) })}</div>
          <div>{t('drawer.updatedAt', { when: formatDateTime(offer.updatedAt) })}</div>
        </dl>
      </aside>
    </dialog>
  )
}
