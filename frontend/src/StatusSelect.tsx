import type { JobOffer, OfferStatus } from './api'
import { OFFER_STATUSES, STATUS_META } from './offerStatus'

export function StatusSelect({
  offer,
  onStatusChange,
}: {
  offer: JobOffer
  onStatusChange: (status: OfferStatus, offerId: string) => void
}) {
  return (
    <select
      className={`rounded-lg border px-2 py-0.5 text-[11px] font-semibold focus:ring-1 focus:ring-[var(--brand)] focus:outline-none ${STATUS_META[offer.status].pill}`}
      value={offer.status}
      aria-label={`Status: ${offer.title}`}
      onChange={(e) => onStatusChange(e.target.value as OfferStatus, offer.id)}
    >
      {OFFER_STATUSES.map((s) => (
        <option key={s} value={s}>
          {STATUS_META[s].label}
        </option>
      ))}
    </select>
  )
}
