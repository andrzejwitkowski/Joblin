import { useTranslation } from 'react-i18next'
import type { JobOffer, OfferStatus } from './api'
import { OFFER_STATUSES, STATUS_META, statusLabel } from './offerStatus'

export function StatusSelect({
  offer,
  onStatusChange,
}: {
  offer: JobOffer
  onStatusChange: (status: OfferStatus, offerId: string) => void
}) {
  const { t } = useTranslation()
  return (
    <select
      className={`rounded-lg border px-2 py-0.5 text-[11px] font-semibold focus:ring-1 focus:ring-brand focus:outline-none ${STATUS_META[offer.status].pill}`}
      value={offer.status}
      aria-label={`${t('common.status')}: ${offer.title}`}
      onChange={(e) => onStatusChange(e.target.value as OfferStatus, offer.id)}
    >
      {OFFER_STATUSES.map((s) => (
        <option key={s} value={s}>
          {statusLabel(s, t)}
        </option>
      ))}
    </select>
  )
}
