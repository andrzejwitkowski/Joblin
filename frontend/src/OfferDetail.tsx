import { ArrowLeft, ExternalLink } from 'lucide-react'
import type { ReactNode } from 'react'
import type { JobOffer, OfferStatus } from './api'
import { OfferIcon } from './offerIcons'
import { STATUS_META } from './offerStatus'
import { OfferSectionsView } from './offerSections'
import { SourceBotIcon } from './SourceBotIcon'
import { StatusSelect } from './StatusSelect'

function MetaItem({
  icon,
  children,
  emphasize,
}: {
  icon: string
  children: ReactNode
  emphasize?: boolean
}) {
  return (
    <span className={`inline-flex items-center gap-1 ${emphasize ? 'text-slate-800' : ''}`}>
      <OfferIcon name={icon} size={15} className={emphasize ? 'text-amber-600' : 'text-slate-400'} />
      {children}
    </span>
  )
}

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
    <div className="space-y-6 px-5 py-6">
      <nav className="flex flex-wrap items-center gap-2 text-sm text-slate-500">
        <button
          type="button"
          className="inline-flex items-center gap-1 text-[var(--brand)] hover:underline"
          onClick={onBack}
        >
          <ArrowLeft size={16} aria-hidden />
          Tablica ofert
        </button>
        <span aria-hidden className="text-slate-300">
          /
        </span>
        <span className="truncate font-medium text-slate-900">{offer.title}</span>
      </nav>

      <header className="rounded-xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)] md:p-6">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div className="min-w-0 space-y-3">
            <div className="flex flex-wrap items-center gap-2.5">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-amber-600">
                <OfferIcon name="corporate_fare" size={22} />
              </span>
              <span className="text-base font-semibold tracking-tight text-slate-900">{offer.company}</span>
              {offer.employmentLabel && (
                <>
                  <span className="text-slate-300" aria-hidden>
                    |
                  </span>
                  <span className="rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-600">
                    {offer.employmentLabel}
                  </span>
                </>
              )}
            </div>
            <h2 className="text-2xl font-semibold tracking-tight text-slate-900 md:text-3xl">{offer.title}</h2>
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1.5 text-sm text-slate-500">
              {offer.salary && (
                <MetaItem icon="monetization_on" emphasize>
                  <span className="font-semibold">{offer.salary}</span>
                </MetaItem>
              )}
              {offer.location && <MetaItem icon="location_on">{offer.location}</MetaItem>}
              {offer.workMode && <MetaItem icon="laptop_mac">{offer.workMode}</MetaItem>}
              <MetaItem icon="schedule">
                Znaleziono {new Date(offer.foundAt).toLocaleString('pl-PL')}
              </MetaItem>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <label className="flex items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm">
              <span className="sr-only">Status</span>
              <StatusSelect offer={offer} onStatusChange={(status) => onStatusChange(status)} />
            </label>
            <a
              className="inline-flex items-center gap-1.5 rounded-lg bg-[var(--brand)] px-4 py-2 text-sm font-medium text-white hover:bg-[var(--brand-600)]"
              href={offer.sourceUrl}
              target="_blank"
              rel="noreferrer"
            >
              Aplikuj / otwórz źródło
              <ExternalLink size={16} aria-hidden />
            </a>
          </div>
        </div>
      </header>

      <div className="grid gap-6 lg:grid-cols-12 lg:items-start">
        <OfferSectionsView offer={offer} className="lg:col-span-8" />
        <aside className="rounded-xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)] lg:col-span-4">
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Metadane i źródło</h3>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between gap-3">
              <dt className="text-slate-500">Status</dt>
              <dd className="font-medium text-slate-800">{STATUS_META[offer.status].label}</dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-slate-500">Bot</dt>
              <dd className="inline-flex items-center gap-1.5 font-mono text-xs text-slate-800">
                <SourceBotIcon bot={offer.sourceBot} size={14} />
                {offer.sourceBot}
              </dd>
            </div>
            <div className="flex justify-between gap-3">
              <dt className="text-slate-500">Aktualizacja</dt>
              <dd className="font-mono text-xs text-slate-700">
                {new Date(offer.updatedAt).toLocaleString('pl-PL')}
              </dd>
            </div>
          </dl>
          <div className="mt-4 rounded-xl border border-slate-200 bg-slate-50 p-3">
            <span className="block text-[10px] font-semibold tracking-wider text-slate-400 uppercase">
              Kanoniczny URL
            </span>
            <a
              className="mt-1 block break-all font-mono text-xs text-[var(--brand)] underline"
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
