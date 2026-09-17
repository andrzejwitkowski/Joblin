import type { ReactNode } from 'react'
import type { JobOffer, OfferSection, OfferTone, PillItem, SpecItem, TitledItem } from './api'
import { OfferIcon } from './offerIcons'

const TONE_DOT: Record<OfferTone, string> = {
  DEFAULT: 'bg-slate-400',
  PRIMARY: 'bg-[var(--brand)]',
  SECONDARY: 'bg-slate-800',
  TERTIARY: 'bg-amber-500',
}

function truncate(text: string, max: number) {
  return text.length <= max ? text : `${text.slice(0, max - 1)}…`
}

function previewNarrative(paragraphs: string[], max = 360) {
  return truncate(paragraphs.join('\n\n'), max)
}

function SectionTitle({ title, icon }: { title: string; icon?: string | null }) {
  return (
    <h3 className="flex items-center gap-2 text-base font-semibold tracking-tight text-slate-900">
      {icon && (
        <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-[var(--brand)]">
          <OfferIcon name={icon} size={16} />
        </span>
      )}
      {title}
    </h3>
  )
}

function SpecsGrid({ items, cols }: { items: SpecItem[]; cols: string }) {
  return (
    <div className={`grid gap-2.5 ${cols}`}>
      {items.map((item) => (
        <div
          key={`${item.label}:${item.value}`}
          className="rounded-xl border border-slate-200 bg-white p-3.5 shadow-sm"
        >
          <div className="mb-2 flex items-center gap-2">
            {item.icon && (
              <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-[var(--brand)]">
                <OfferIcon name={item.icon} size={15} />
              </span>
            )}
            <span className="text-[10px] font-semibold tracking-wider text-slate-400 uppercase">
              {item.label}
            </span>
          </div>
          <div className="text-sm font-semibold text-slate-900">{item.value}</div>
          {item.hint && <span className="mt-0.5 block text-xs text-slate-500">{item.hint}</span>}
        </div>
      ))}
    </div>
  )
}

function PillChips({ items, showBadge }: { items: PillItem[]; showBadge?: boolean }) {
  return (
    <div className="flex flex-wrap gap-1.5">
      {items.map((p) => (
        <span
          key={p.label}
          className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-xs text-slate-700 shadow-sm"
        >
          {p.icon ? (
            <OfferIcon name={p.icon} size={12} className="text-slate-500" />
          ) : (
            <span className={`h-1.5 w-1.5 rounded-full ${TONE_DOT[p.tone ?? 'DEFAULT']}`} aria-hidden />
          )}
          <span className={p.tone === 'PRIMARY' ? 'font-semibold' : undefined}>{p.label}</span>
          {showBadge && p.badge && (
            <span className="rounded bg-blue-50 px-1 py-0.5 text-[10px] text-[var(--brand)]">{p.badge}</span>
          )}
        </span>
      ))}
    </div>
  )
}

function TitledGrid({ items }: { items: TitledItem[] }) {
  return (
    <div className="grid gap-2.5 sm:grid-cols-2">
      {items.map((item) => (
        <div
          key={item.title}
          className="rounded-xl border border-slate-200 bg-white p-3.5 shadow-sm"
        >
          <div className="flex items-start gap-2">
            {item.icon && (
              <span className="mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-[var(--brand)]">
                <OfferIcon name={item.icon} size={15} />
              </span>
            )}
            <div className="min-w-0">
              <div className="text-sm font-semibold text-slate-900">{item.title}</div>
              {item.body && <p className="mt-1 text-xs leading-relaxed text-slate-500">{item.body}</p>}
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

function LegacyBody({ offer }: { offer: JobOffer }) {
  return (
    <>
      <p className="text-sm leading-relaxed whitespace-pre-wrap text-slate-700">{offer.description}</p>
      {offer.tags.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {offer.tags.map((t) => (
            <span
              key={t}
              className="rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1 text-xs font-medium text-slate-700"
            >
              {t}
            </span>
          ))}
        </div>
      )}
    </>
  )
}

function SectionCard({ children, className }: { children: ReactNode; className: string }) {
  return <div className={`rounded-xl border border-slate-200 p-4 shadow-sm ${className}`}>{children}</div>
}

function FullSection({ section }: { section: OfferSection }) {
  switch (section.type) {
    case 'SPECS':
      return <SpecsGrid items={section.items} cols="grid-cols-2 sm:grid-cols-4" />
    case 'SOURCE':
      return (
        <SectionCard className="flex flex-col gap-3 bg-slate-50 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex min-w-0 items-start gap-3">
            <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white text-[var(--brand)] shadow-sm">
              <OfferIcon name={section.icon ?? 'travel_explore'} size={20} />
            </span>
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-2 text-xs tracking-wider text-slate-400 uppercase">
                Zweryfikowane źródło zewnętrzne
                <span className="rounded-md bg-white px-1.5 py-0.5 font-mono text-[11px] normal-case text-[var(--brand)] shadow-sm">
                  {section.engineLabel}
                  {section.scraperId ? ` · ${section.scraperId}` : ''}
                </span>
              </div>
              <p className="mt-1 truncate font-mono text-xs text-slate-500">{section.url}</p>
            </div>
          </div>
          <div className="flex shrink-0 gap-2">
            <button
              type="button"
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 shadow-sm"
              onClick={() => void navigator.clipboard.writeText(section.url)}
            >
              <OfferIcon name="content_copy" size={14} />
              Kopiuj link
            </button>
            <a
              className="inline-flex items-center gap-1.5 rounded-lg bg-[var(--brand)] px-3 py-1.5 text-sm text-white hover:bg-[var(--brand-600)]"
              href={section.url}
              target="_blank"
              rel="noreferrer"
            >
              Otwórz stronę
              <OfferIcon name="open_in_new" size={14} />
            </a>
          </div>
        </SectionCard>
      )
    case 'NARRATIVE':
      return (
        <SectionCard className="space-y-3 bg-white">
          <SectionTitle title={section.title} icon={section.icon} />
          {section.paragraphs.map((p, i) => (
            <p key={i} className="text-sm leading-relaxed whitespace-pre-wrap text-slate-600">
              {p}
            </p>
          ))}
        </SectionCard>
      )
    case 'PILLS':
      return (
        <SectionCard className="space-y-3 bg-white">
          <SectionTitle title={section.title} icon={section.icon} />
          <PillChips items={section.items} showBadge />
        </SectionCard>
      )
    case 'CHECKLIST':
    case 'CARDS':
      return (
        <SectionCard className="space-y-3 bg-white">
          <SectionTitle title={section.title} icon={section.icon} />
          <TitledGrid items={section.items} />
        </SectionCard>
      )
  }
}

function PreviewSection({ section }: { section: OfferSection }) {
  switch (section.type) {
    case 'SPECS':
      return <SpecsGrid items={section.items.slice(0, 4)} cols="grid-cols-2" />
    case 'SOURCE':
      return (
        <a
          className="inline-flex items-center gap-1.5 text-sm font-medium text-[var(--brand)] underline"
          href={section.url}
          target="_blank"
          rel="noreferrer"
        >
          <OfferIcon name={section.icon ?? 'link'} size={14} />
          {section.engineLabel}
        </a>
      )
    case 'NARRATIVE':
      return (
        <div>
          <SectionTitle title={section.title} icon={section.icon} />
          <p className="mt-1 text-sm leading-relaxed whitespace-pre-wrap text-slate-600">
            {previewNarrative(section.paragraphs)}
          </p>
        </div>
      )
    case 'PILLS':
      return (
        <div className="space-y-2">
          <SectionTitle title={section.title} icon={section.icon} />
          <PillChips items={section.items} />
        </div>
      )
    case 'CHECKLIST':
    case 'CARDS':
      return (
        <p className="flex items-center gap-1.5 text-sm text-slate-500">
          {section.icon && <OfferIcon name={section.icon} size={14} className="text-slate-400" />}
          {section.title} · {section.items.length} pozycji
        </p>
      )
  }
}

export function OfferSectionsView({ offer, className }: { offer: JobOffer; className?: string }) {
  if (offer.sections.length === 0) {
    return (
      <div className={className}>
        <LegacyBody offer={offer} />
      </div>
    )
  }
  return (
    <div className={['space-y-5', className].filter(Boolean).join(' ')}>
      {offer.sections.map((section, i) => (
        <FullSection key={`${section.type}-${i}`} section={section} />
      ))}
    </div>
  )
}

export function OfferSectionsPreview({ offer }: { offer: JobOffer }) {
  if (offer.sections.length === 0) return <LegacyBody offer={offer} />
  return (
    <div className="mt-4 space-y-4">
      {offer.sections.map((section, i) => (
        <PreviewSection key={`${section.type}-${i}`} section={section} />
      ))}
    </div>
  )
}

if (import.meta.env.DEV) {
  console.assert(previewNarrative(['About Us:-', 'Body text here'], 20).startsWith('About Us:-'))
  console.assert(previewNarrative(['Only one.'], 180) === 'Only one.')
}
