import { Bot, BriefcaseBusiness } from 'lucide-react'
import type { JobOffer, OfferStatus } from './api'

const COLUMNS: { status: OfferStatus; label: string }[] = [
  { status: 'NEW', label: 'New' },
  { status: 'INTERESTED', label: 'Interested' },
  { status: 'APPLIED', label: 'Applied' },
  { status: 'NOT_FOR_ME', label: 'Not for me' },
]

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
      {COLUMNS.map((col) => (
        <section
          key={col.status}
          className="flex min-h-[420px] flex-col rounded-lg border border-[var(--line)] bg-[var(--panel)]/70"
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault()
            const id = e.dataTransfer.getData('text/offer-id')
            if (id) onStatusChange(col.status, id)
          }}
        >
          <h2 className="border-b border-[var(--line)] px-3 py-2 text-sm font-medium tracking-wide">
            {col.label}
            <span className="ml-2 text-[var(--muted)]">{byStatus[col.status].length}</span>
          </h2>
          <div className="flex flex-1 flex-col gap-2 p-2">
            {byStatus[col.status].map((offer) => (
              <article
                key={offer.id}
                draggable
                onDragStart={(e) => e.dataTransfer.setData('text/offer-id', offer.id)}
                onClick={() => onSelect(offer)}
                className="cursor-grab rounded-md border border-[var(--line)] bg-white p-3 shadow-sm active:cursor-grabbing"
              >
                <div className="mb-1 flex items-start justify-between gap-2">
                  <h3 className="font-medium leading-snug">{offer.title}</h3>
                  {offer.sourceBot === 'HERMES' ? (
                    <Bot size={16} className="shrink-0 text-[var(--muted)]" />
                  ) : (
                    <BriefcaseBusiness size={16} className="shrink-0 text-[var(--muted)]" />
                  )}
                </div>
                <p className="text-sm text-[var(--muted)]">{offer.company}</p>
              </article>
            ))}
          </div>
        </section>
      ))}
    </div>
  )
}
