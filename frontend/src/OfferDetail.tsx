import type { JobOffer, OfferStatus } from './api'
import { STATUS_META } from './offerStatus'
import { OfferSectionsView } from './offerSections'
import { StatusSelect } from './StatusSelect'

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
        <button type="button" className="text-[var(--brand)] hover:underline" onClick={onBack}>
          ← Tablica ofert
        </button>
        <span aria-hidden>/</span>
        <span className="truncate font-medium text-slate-900">{offer.title}</span>
      </nav>

      <header className="rounded-xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)] md:p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="min-w-0 space-y-2">
            <p className="text-sm text-slate-500">{offer.company}</p>
            <h2 className="text-2xl font-semibold tracking-tight text-slate-900 md:text-3xl">{offer.title}</h2>
            <div className="flex flex-wrap gap-x-3 gap-y-1 text-sm text-slate-500">
              {offer.salary && <span className="font-semibold text-slate-800">{offer.salary}</span>}
              {offer.location && <span>{offer.location}</span>}
              {offer.workMode && <span>{offer.workMode}</span>}
              {offer.employmentLabel && <span>{offer.employmentLabel}</span>}
              <span>Znaleziono {new Date(offer.foundAt).toLocaleString('pl-PL')}</span>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <label className="flex items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm">
              <span className="sr-only">Status</span>
              <StatusSelect
                offer={offer}
                onStatusChange={(status) => onStatusChange(status)}
              />
            </label>
            <a
              className="rounded-lg bg-[var(--brand)] px-4 py-2 text-sm font-medium text-white hover:bg-[var(--brand-600)]"
              href={offer.sourceUrl}
              target="_blank"
              rel="noreferrer"
            >
              Aplikuj / otwórz źródło
            </a>
          </div>
        </div>
      </header>

      <div className="grid gap-6 lg:grid-cols-12 lg:items-start">
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)] md:p-6 lg:col-span-8">
          <OfferSectionsView offer={offer} />
        </div>
        <aside className="rounded-xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)] lg:col-span-4">
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Metadane i źródło</h3>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between gap-3">
              <dt className="text-slate-500">Status</dt>
              <dd>{STATUS_META[offer.status].label}</dd>
            </div>
            <div className="flex justify-between gap-3">
              <dt className="text-slate-500">Bot</dt>
              <dd className="font-mono text-xs">{offer.sourceBot}</dd>
            </div>
            <div className="flex justify-between gap-3">
              <dt className="text-slate-500">Aktualizacja</dt>
              <dd className="font-mono text-xs">{new Date(offer.updatedAt).toLocaleString('pl-PL')}</dd>
            </div>
          </dl>
          <div className="mt-4 rounded-lg bg-slate-50 p-3">
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
