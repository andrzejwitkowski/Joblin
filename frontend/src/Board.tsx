import { Bot, BriefcaseBusiness } from 'lucide-react'
import { OFFER_STATUSES, type JobOffer, type OfferStatus } from './api'

function groupByStatus(offers: JobOffer[]): Record<OfferStatus, JobOffer[]> {
  const map: Record<OfferStatus, JobOffer[]> = {
    NEW: [],
    INTERESTED: [],
    APPLIED: [],
    NOT_FOR_ME: [],
  }
  for (const o of offers) map[o.status].push(o)
  return map
}

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

  return (
    <div className="grid flex-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
      {OFFER_STATUSES.map((col) => (
        <section
          key={col.value}
          className="flex min-h-[420px] flex-col rounded-lg border border-[var(--line)] bg-[var(--panel)]/70"
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault()
            const id = e.dataTransfer.getData('text/offer-id')
            if (id) onStatusChange(col.value, id)
          }}
        >
          <h2 className="border-b border-[var(--line)] px-3 py-2 text-sm font-medium tracking-wide">
            {col.label}
            <span className="ml-2 text-[var(--muted)]">{byStatus[col.value].length}</span>
          </h2>
          <div className="flex flex-1 flex-col gap-2 p-2">
            {byStatus[col.value].map((offer) => (
              <article
                key={offer.id}
                draggable
                onDragStart={(e) => e.dataTransfer.setData('text/offer-id', offer.id)}
                className="rounded-md border border-[var(--line)] bg-white p-3 shadow-sm"
              >
                <div className="mb-1 flex items-start justify-between gap-2">
                  <button
                    type="button"
                    className="cursor-pointer text-left font-medium leading-snug hover:underline"
                    onClick={() => onSelect(offer)}
                  >
                    {offer.title}
                  </button>
                  {offer.sourceBot === 'HERMES' ? (
                    <Bot size={16} className="shrink-0 text-[var(--muted)]" aria-hidden />
                  ) : (
                    <BriefcaseBusiness size={16} className="shrink-0 text-[var(--muted)]" aria-hidden />
                  )}
                </div>
                <p className="text-sm text-[var(--muted)]">{offer.company}</p>
                <label className="mt-2 flex items-center gap-2 text-xs text-[var(--muted)]">
                  Status
                  <select
                    className="rounded border border-[var(--line)] bg-white px-1 py-0.5 text-xs text-[var(--ink)]"
                    value={offer.status}
                    aria-label={`Status for ${offer.title}`}
                    onChange={(e) => onStatusChange(e.target.value as OfferStatus, offer.id)}
                  >
                    {OFFER_STATUSES.map((c) => (
                      <option key={c.value} value={c.value}>
                        {c.label}
                      </option>
                    ))}
                  </select>
                </label>
              </article>
            ))}
          </div>
        </section>
      ))}
    </div>
  )
}
