import { useState } from 'react'
import type { JobOffer, OfferStatus } from './api'
import { countByStatus, OFFER_STATUSES, STATUS_META } from './offerStatus'
import { relativeTime } from './relativeTime'
import { SourceBotIcon } from './SourceBotIcon'
import { StatusSelect } from './StatusSelect'

type Tab = 'all' | OfferStatus

export function OfferList({
  offers,
  onSelect,
  onStatusChange,
}: {
  offers: JobOffer[]
  onSelect: (offer: JobOffer) => void
  onStatusChange: (status: OfferStatus, offerId: string) => void
}) {
  const [tab, setTab] = useState<Tab>('all')
  const counts = countByStatus(offers)
  const rows = tab === 'all' ? offers : offers.filter((o) => o.status === tab)

  return (
    <div className="flex min-h-0 flex-1 flex-col overflow-hidden px-5 pb-4">
      <div className="flex flex-wrap items-center gap-4 border-b border-slate-200 px-1 pt-1 sm:gap-6">
        <TabButton active={tab === 'all'} count={offers.length} onClick={() => setTab('all')}>
          Wszystkie
        </TabButton>
        {OFFER_STATUSES.map((status) => (
          <TabButton
            key={status}
            active={tab === status}
            count={counts[status]}
            dot={STATUS_META[status].dot}
            onClick={() => setTab(status)}
          >
            {STATUS_META[status].label}
          </TabButton>
        ))}
      </div>

      <div className="mt-3 min-h-0 flex-1 overflow-auto rounded-xl border border-slate-200 bg-white shadow-sm">
        <table className="w-full border-collapse text-left text-xs">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50/80 text-[10px] font-semibold tracking-wide text-slate-500 uppercase">
              <th className="min-w-[280px] px-4 py-3">Rola i Firma</th>
              <th className="min-w-[160px] px-4 py-3">Status</th>
              <th className="min-w-[140px] px-4 py-3">Wynagrodzenie</th>
              <th className="min-w-[180px] px-4 py-3">Technologie</th>
              <th className="min-w-[100px] px-4 py-3">Aktywność</th>
              <th className="min-w-[90px] px-4 py-3 text-right">Akcje</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {rows.map((offer) => (
              <OfferRow key={offer.id} offer={offer} onSelect={onSelect} onStatusChange={onStatusChange} />
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-slate-400">
                  Brak ofert dla wybranych filtrów
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function OfferRow({
  offer,
  onSelect,
  onStatusChange,
}: {
  offer: JobOffer
  onSelect: (offer: JobOffer) => void
  onStatusChange: (status: OfferStatus, offerId: string) => void
}) {
  const rejected = offer.status === 'NOT_FOR_ME'
  return (
    <tr className={`hover:bg-slate-50 ${rejected ? 'bg-slate-50/50 opacity-75' : ''}`}>
      <td className="px-4 py-3.5">
        <div className="flex items-start gap-2.5">
          <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-blue-100 text-xs font-bold text-blue-700">
            {(offer.company.trim()[0] ?? '?').toUpperCase()}
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <button
                type="button"
                className={`leading-tight font-bold hover:text-[var(--brand)] ${
                  rejected ? 'text-slate-600 line-through' : 'text-slate-900'
                }`}
                onClick={() => onSelect(offer)}
              >
                {offer.title}
              </button>
              <span className="text-slate-400" title={offer.sourceBot}>
                <SourceBotIcon bot={offer.sourceBot} size={14} />
              </span>
            </div>
            <p className={`mt-0.5 text-[11px] ${rejected ? 'text-slate-400' : 'text-slate-500'}`}>{offer.company}</p>
          </div>
        </div>
      </td>
      <td className="px-4 py-3.5">
        <StatusSelect offer={offer} onStatusChange={onStatusChange} />
      </td>
      <td className={`px-4 py-3.5 font-bold ${rejected ? 'text-slate-400 line-through' : 'text-slate-800'}`}>
        {offer.salary ?? '—'}
      </td>
      <td className="px-4 py-3.5">
        <div className={`flex flex-wrap gap-1 ${rejected ? 'opacity-70' : ''}`}>
          {offer.tags.length === 0 && <span className="text-slate-400">—</span>}
          {offer.tags.slice(0, 5).map((t) => (
            <span key={t} className="rounded border border-slate-200 bg-slate-100 px-2 py-0.5 text-[11px] text-slate-700">
              {t}
            </span>
          ))}
        </div>
      </td>
      <td className={`px-4 py-3.5 text-[11px] ${rejected ? 'text-slate-400' : 'text-slate-500'}`}>
        {relativeTime(offer.foundAt)}
      </td>
      <td className="px-4 py-3.5 text-right">
        {rejected ? (
          <button
            type="button"
            className="rounded border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-500 hover:text-slate-800"
            onClick={() => onStatusChange('NEW', offer.id)}
          >
            Przywróć
          </button>
        ) : (
          <button
            type="button"
            className="rounded border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-700 hover:text-blue-600"
            onClick={() => onSelect(offer)}
          >
            Szczegóły
          </button>
        )}
      </td>
    </tr>
  )
}

function TabButton({
  active,
  count,
  dot,
  onClick,
  children,
}: {
  active: boolean
  count: number
  dot?: string
  onClick: () => void
  children: string
}) {
  return (
    <button
      type="button"
      className={`flex items-center gap-1.5 border-b-2 pb-2.5 text-xs ${
        active
          ? 'border-[var(--brand)] font-bold text-[var(--brand)]'
          : 'border-transparent font-semibold text-slate-500 hover:text-slate-800'
      }`}
      onClick={onClick}
    >
      {dot && <span className={`h-2 w-2 rounded-full ${dot}`} />}
      <span>{children}</span>
      <span
        className={`rounded-full px-1.5 text-[10px] ${
          active ? 'bg-blue-100 text-[var(--brand)]' : 'bg-slate-200/70 text-slate-600'
        }`}
      >
        {count}
      </span>
    </button>
  )
}
