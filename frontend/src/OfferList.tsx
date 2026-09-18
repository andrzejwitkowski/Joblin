import { useState } from 'react'
import type { JobOffer, OfferStatus } from './api'
import { countByStatus, fadeOpacity, isTerminal, OFFER_STATUSES, STATUS_META } from './offerStatus'
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
      <div className="flex flex-wrap items-center gap-4 border-b border-border px-1 pt-1 sm:gap-6">
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

      <div className="mt-3 min-h-0 flex-1 overflow-auto rounded-xl border border-border bg-surface shadow-sm">
        <table className="w-full border-collapse text-left text-xs">
          <thead>
            <tr className="border-b border-border bg-surface-muted/80 text-[10px] font-semibold tracking-wide text-ink-muted uppercase">
              <th className="min-w-[280px] px-4 py-3">Rola i Firma</th>
              <th className="min-w-[160px] px-4 py-3">Status</th>
              <th className="min-w-[140px] px-4 py-3">Wynagrodzenie</th>
              <th className="min-w-[180px] px-4 py-3">Technologie</th>
              <th className="min-w-[100px] px-4 py-3">Aktywność</th>
              <th className="min-w-[90px] px-4 py-3 text-right">Akcje</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {rows.map((offer) => (
              <OfferRow key={offer.id} offer={offer} onSelect={onSelect} onStatusChange={onStatusChange} />
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-ink-muted">
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
  const terminal = isTerminal(offer.status)
  const opacity = terminal ? fadeOpacity(offer.fadeStartedAt) : 1
  return (
    <tr
      style={terminal ? { opacity } : undefined}
      className={`hover:bg-surface-muted ${terminal ? 'bg-surface-muted/50' : ''}`}
    >
      <td className="px-4 py-3.5">
        <div className="flex items-start gap-2.5">
          <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-blue-100 text-xs font-bold text-blue-700 dark:bg-blue-950/50 dark:text-blue-300">
            {(offer.company.trim()[0] ?? '?').toUpperCase()}
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <button
                type="button"
                className={`leading-tight font-bold hover:text-brand ${
                  terminal ? 'text-ink-muted line-through' : 'text-ink'
                }`}
                onClick={() => onSelect(offer)}
              >
                {offer.title}
              </button>
              <span className="text-ink-muted" title={offer.sourceBot}>
                <SourceBotIcon bot={offer.sourceBot} size={14} />
              </span>
            </div>
            <p className="mt-0.5 text-[11px] text-ink-muted">{offer.company}</p>
          </div>
        </div>
      </td>
      <td className="px-4 py-3.5">
        <StatusSelect offer={offer} onStatusChange={onStatusChange} />
      </td>
      <td className={`px-4 py-3.5 font-bold ${terminal ? 'text-ink-muted line-through' : 'text-ink'}`}>
        {offer.salary ?? '—'}
      </td>
      <td className="px-4 py-3.5">
        <div className={`flex flex-wrap gap-1 ${terminal ? 'opacity-70' : ''}`}>
          {offer.tags.length === 0 && <span className="text-ink-muted">—</span>}
          {offer.tags.slice(0, 5).map((t) => (
            <span
              key={t}
              className="rounded border border-border bg-surface-muted px-2 py-0.5 text-[11px] text-ink"
            >
              {t}
            </span>
          ))}
        </div>
      </td>
      <td className="px-4 py-3.5 text-[11px] text-ink-muted">{relativeTime(offer.foundAt)}</td>
      <td className="px-4 py-3.5 text-right">
        {terminal ? (
          <button
            type="button"
            className="rounded border border-border bg-surface px-2.5 py-1 text-xs text-ink-muted hover:text-ink"
            onClick={() => onStatusChange('NEW', offer.id)}
          >
            Przywróć
          </button>
        ) : (
          <button
            type="button"
            className="rounded border border-border bg-surface px-2.5 py-1 text-xs text-ink hover:text-brand"
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
          ? 'border-brand font-bold text-brand'
          : 'border-transparent font-semibold text-ink-muted hover:text-ink'
      }`}
      onClick={onClick}
    >
      {dot && <span className={`h-2 w-2 rounded-full ${dot}`} />}
      <span>{children}</span>
      <span
        className={`rounded-full px-1.5 text-[10px] ${
          active
            ? 'bg-blue-100 text-brand dark:bg-blue-950/50'
            : 'bg-surface-muted text-ink-muted'
        }`}
      >
        {count}
      </span>
    </button>
  )
}
