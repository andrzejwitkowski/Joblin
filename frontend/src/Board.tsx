import { useRef } from 'react'
import type { JobOffer, OfferStatus } from './api'
import { fadeOpacity, groupByStatus, isTerminal, OFFER_STATUSES, STATUS_META } from './offerStatus'
import { relativeTime } from './relativeTime'
import { SourceBotIcon } from './SourceBotIcon'
import { StatusSelect } from './StatusSelect'

export function Board({
  offers,
  onSelect,
  onStatusChange,
}: {
  offers: JobOffer[]
  onSelect: (offer: JobOffer) => void
  onStatusChange: (status: OfferStatus, offerId: string) => void
}) {
  const byStatus = groupByStatus(offers)
  const draggingId = useRef<string | null>(null)

  function handleDrop(e: React.DragEvent, status: OfferStatus) {
    e.preventDefault()
    const id = draggingId.current ?? (e.dataTransfer.getData('text/plain') || null)
    draggingId.current = null
    if (id) onStatusChange(status, id)
  }

  return (
    <main className="kanban-scroll flex-1 overflow-x-auto overflow-y-hidden bg-paper p-6">
      <div className="flex h-full min-h-[420px] min-w-max gap-5 pb-4">
        {OFFER_STATUSES.map((status) => {
          const meta = STATUS_META[status]
          const items = byStatus[status]
          return (
            <section
              key={status}
              className="flex h-full w-80 flex-col rounded-xl border border-border bg-surface-muted"
            >
              <div className="flex flex-shrink-0 items-center gap-2 rounded-t-xl border-b border-border bg-surface/70 p-3.5">
                <span className={`h-2.5 w-2.5 rounded-full ${meta.dot}`} />
                <h2 className="text-xs font-semibold tracking-wide text-ink uppercase">{meta.label}</h2>
                <span className={`rounded-full px-2 py-0.5 text-[11px] font-bold ${meta.badge}`}>{items.length}</span>
              </div>
              <div
                className="kanban-scroll flex min-h-0 flex-1 flex-col space-y-3.5 overflow-y-auto p-3"
                onDragOver={(e) => e.preventDefault()}
                onDrop={(e) => handleDrop(e, status)}
              >
                {items.map((offer) => (
                  <OfferCard
                    key={offer.id}
                    offer={offer}
                    onSelect={onSelect}
                    onStatusChange={onStatusChange}
                    onBeginDrag={(id) => {
                      draggingId.current = id
                    }}
                    onEndDrag={() => {
                      draggingId.current = null
                    }}
                  />
                ))}
              </div>
            </section>
          )
        })}
      </div>
    </main>
  )
}

function OfferCard({
  offer,
  onSelect,
  onStatusChange,
  onBeginDrag,
  onEndDrag,
}: {
  offer: JobOffer
  onSelect: (offer: JobOffer) => void
  onStatusChange: (status: OfferStatus, offerId: string) => void
  onBeginDrag: (id: string) => void
  onEndDrag: () => void
}) {
  const terminal = isTerminal(offer.status)
  const opacity = terminal ? fadeOpacity(offer.fadeStartedAt) : 1
  return (
    <article
      draggable
      onDragStart={(e) => {
        if ((e.target as HTMLElement).closest('select, button, label, option')) {
          e.preventDefault()
          return
        }
        onBeginDrag(offer.id)
        e.dataTransfer.effectAllowed = 'move'
        e.dataTransfer.setData('text/plain', offer.id)
      }}
      onDragEnd={onEndDrag}
      onDragOver={(e) => e.preventDefault()}
      style={terminal ? { opacity } : undefined}
      className="group cursor-grab rounded-xl border border-border bg-surface p-3.5 shadow-[var(--shadow-card)] transition duration-200 hover:shadow-[var(--shadow-card-hover)]"
    >
      <div className="flex items-start justify-between gap-2">
        <div>
          <span className="mb-1.5 inline-flex items-center rounded border border-blue-100 bg-blue-50 px-1.5 py-0.5 text-[10px] font-semibold text-brand dark:border-blue-800 dark:bg-blue-950/50">
            {offer.company}
          </span>
          <h3
            className={`text-sm leading-snug font-semibold group-hover:text-brand ${
              terminal ? 'text-ink-muted line-through' : 'text-ink'
            }`}
          >
            <button type="button" className="cursor-pointer text-left" onClick={() => onSelect(offer)}>
              {offer.title}
            </button>
          </h3>
        </div>
        <span className="p-1 text-ink-muted" title={offer.sourceBot}>
          <SourceBotIcon bot={offer.sourceBot} />
        </span>
      </div>

      {offer.salary && (
        <p
          className={`mt-2.5 border-t border-border pt-2 text-xs font-bold ${
            terminal ? 'text-ink-muted line-through' : 'text-ink'
          }`}
        >
          {offer.salary}
        </p>
      )}

      {offer.tags.length > 0 && (
        <div className="mt-2.5 flex flex-wrap gap-1">
          {offer.tags.slice(0, 4).map((t) => (
            <span
              key={t}
              className="rounded bg-surface-muted px-1.5 py-0.5 text-[10px] font-medium text-ink-muted"
            >
              {t}
            </span>
          ))}
        </div>
      )}

      <div className="mt-3.5 flex items-center justify-between border-t border-border pt-2.5">
        <label className="flex items-center gap-1.5">
          <span className="text-[11px] font-medium text-ink-muted">Status:</span>
          <StatusSelect offer={offer} onStatusChange={onStatusChange} />
        </label>
        {terminal ? (
          <button
            type="button"
            className="text-xs text-ink-muted underline hover:text-ink"
            onClick={() => onStatusChange('NEW', offer.id)}
          >
            Przywróć
          </button>
        ) : (
          <span className="text-[10px] text-ink-muted">{relativeTime(offer.foundAt)}</span>
        )}
      </div>
    </article>
  )
}
