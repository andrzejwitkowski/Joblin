import { OFFER_STATUSES, type JobOffer, type OfferStatus } from './api'
import { OfferSectionsView } from './offerSections'

export function OfferDetail({
  offer,
  onBack,
  onStatusChange,
}: {
  offer: JobOffer
  onBack: () => void
  onStatusChange: (status: OfferStatus) => void
}) {
  return (
    <div className="space-y-6">
      <nav className="flex flex-wrap items-center gap-2 text-sm text-[var(--muted)]">
        <button type="button" className="text-[var(--accent)] hover:underline" onClick={onBack}>
          ← Offer board
        </button>
        <span aria-hidden>/</span>
        <span className="truncate font-medium text-[var(--ink)]">{offer.title}</span>
      </nav>

      <header className="rounded-xl border border-[var(--line)] bg-[var(--panel)]/50 p-5 md:p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="min-w-0 space-y-2">
            <p className="text-sm text-[var(--muted)]">{offer.company}</p>
            <h2 className="text-2xl font-semibold tracking-tight md:text-3xl">{offer.title}</h2>
            <div className="flex flex-wrap gap-x-3 gap-y-1 text-sm text-[var(--muted)]">
              {offer.salary && <span className="font-medium text-[var(--ink)]">{offer.salary}</span>}
              {offer.location && <span>{offer.location}</span>}
              {offer.workMode && <span>{offer.workMode}</span>}
              {offer.employmentLabel && <span>{offer.employmentLabel}</span>}
              <span>Found {new Date(offer.foundAt).toLocaleString()}</span>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <label className="flex items-center gap-2 rounded-lg border border-[var(--line)] bg-white px-3 py-2 text-sm">
              <span className="sr-only">Status</span>
              <select
                className="bg-transparent outline-none"
                value={offer.status}
                onChange={(e) => onStatusChange(e.target.value as OfferStatus)}
              >
                {OFFER_STATUSES.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </label>
            <a
              className="rounded-lg bg-[var(--accent)] px-4 py-2 text-sm font-medium text-white"
              href={offer.sourceUrl}
              target="_blank"
              rel="noreferrer"
            >
              Apply / open source
            </a>
          </div>
        </div>
      </header>

      <div className="grid gap-6 lg:grid-cols-12 lg:items-start">
        <div className="rounded-xl border border-[var(--line)] bg-white/70 p-5 md:p-6 lg:col-span-8">
          <OfferSectionsView offer={offer} />
        </div>
        <aside className="rounded-xl border border-[var(--line)] bg-white/70 p-5 lg:col-span-4">
          <h3 className="mb-3 text-sm font-semibold">Metadata & source</h3>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between gap-3">
              <dt className="text-[var(--muted)]">Source bot</dt>
              <dd className="font-mono text-xs">{offer.sourceBot}</dd>
            </div>
            <div className="flex justify-between gap-3">
              <dt className="text-[var(--muted)]">Updated</dt>
              <dd className="font-mono text-xs">{new Date(offer.updatedAt).toLocaleString()}</dd>
            </div>
          </dl>
          <div className="mt-4 rounded-lg bg-[var(--panel)]/60 p-3">
            <span className="block text-[10px] font-semibold uppercase tracking-wider text-[var(--muted)]">
              Canonical URL
            </span>
            <a
              className="mt-1 block break-all font-mono text-xs text-[var(--accent)] underline"
              href={offer.sourceUrl}
              target="_blank"
              rel="noreferrer"
            >
              {offer.sourceUrl}
            </a>
          </div>
        </aside>
      </div>
    </div>
  )
}
