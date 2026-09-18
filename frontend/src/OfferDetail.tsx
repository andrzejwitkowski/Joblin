import { ArrowLeft, ExternalLink } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import type { JobOffer, OfferStatus } from './api'
import { formatDateTime } from './i18n/format'
import { OfferIcon } from './offerIcons'
import { statusLabel } from './offerStatus'
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
    <span className={`inline-flex items-center gap-1 ${emphasize ? 'text-ink' : ''}`}>
      <OfferIcon name={icon} size={15} className={emphasize ? 'text-amber-600' : 'text-ink-muted'} />
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
  const { t } = useTranslation()
  return (
    <div className="min-h-0 flex-1 space-y-6 overflow-y-auto px-5 py-6 pb-10">
      <nav className="flex flex-wrap items-center gap-2 text-sm text-ink-muted">
        <button
          type="button"
          className="inline-flex items-center gap-1 text-brand hover:underline"
          onClick={onBack}
        >
          <ArrowLeft size={16} aria-hidden />
          {t('detail.back')}
        </button>
        <span aria-hidden className="text-border">
          /
        </span>
        <span className="truncate font-medium text-ink">{offer.title}</span>
      </nav>

      <header className="rounded-xl border border-border bg-surface p-5 shadow-[var(--shadow-card)] md:p-6">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div className="min-w-0 space-y-3">
            <div className="flex flex-wrap items-center gap-2.5">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-surface-muted text-amber-600">
                <OfferIcon name="corporate_fare" size={22} />
              </span>
              <span className="text-base font-semibold tracking-tight text-ink">{offer.company}</span>
              {offer.employmentLabel && (
                <>
                  <span className="text-border" aria-hidden>
                    |
                  </span>
                  <span className="rounded-md bg-surface-muted px-2 py-0.5 text-[11px] font-medium text-ink-muted">
                    {offer.employmentLabel}
                  </span>
                </>
              )}
            </div>
            <h2 className="text-2xl font-semibold tracking-tight text-ink md:text-3xl">{offer.title}</h2>
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1.5 text-sm text-ink-muted">
              {offer.salary && (
                <MetaItem icon="monetization_on" emphasize>
                  <span className="font-semibold">{offer.salary}</span>
                </MetaItem>
              )}
              {offer.location && <MetaItem icon="location_on">{offer.location}</MetaItem>}
              {offer.workMode && <MetaItem icon="laptop_mac">{offer.workMode}</MetaItem>}
              <MetaItem icon="schedule">{t('detail.foundAt', { when: formatDateTime(offer.foundAt) })}</MetaItem>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <label className="flex items-center gap-2 rounded-lg border border-border bg-surface-muted px-3 py-2 text-sm">
              <span className="sr-only">{t('common.status')}</span>
              <StatusSelect offer={offer} onStatusChange={(status) => onStatusChange(status)} />
            </label>
            <a
              className="inline-flex items-center gap-1.5 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
              href={offer.sourceUrl}
              target="_blank"
              rel="noreferrer"
            >
              {t('detail.apply')}
              <ExternalLink size={16} aria-hidden />
            </a>
          </div>
        </div>
      </header>

      <div className="grid gap-6 lg:grid-cols-12 lg:items-start">
        <OfferSectionsView offer={offer} className="lg:col-span-8" />
        <aside className="rounded-xl border border-border bg-surface p-5 shadow-[var(--shadow-card)] lg:col-span-4">
          <h3 className="mb-3 text-sm font-semibold text-ink">{t('detail.metadata')}</h3>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between gap-3">
              <dt className="text-ink-muted">{t('common.status')}</dt>
              <dd className="font-medium text-ink">{statusLabel(offer.status, t)}</dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-ink-muted">{t('detail.bot')}</dt>
              <dd className="inline-flex items-center gap-1.5 font-mono text-xs text-ink">
                <SourceBotIcon bot={offer.sourceBot} size={14} />
                {offer.sourceBot}
              </dd>
            </div>
            <div className="flex justify-between gap-3">
              <dt className="text-ink-muted">{t('detail.updated')}</dt>
              <dd className="font-mono text-xs text-ink">{formatDateTime(offer.updatedAt)}</dd>
            </div>
          </dl>
          <div className="mt-4 rounded-xl border border-border bg-surface-muted p-3">
            <span className="block text-[10px] font-semibold tracking-wider text-ink-muted uppercase">
              {t('detail.canonicalUrl')}
            </span>
            <a
              className="mt-1 block break-all font-mono text-xs text-brand underline"
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
