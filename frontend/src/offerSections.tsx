import type { JobOffer, OfferSection, OfferTone, PillItem, SpecItem, TitledItem } from './api'

const TONE_DOT: Record<OfferTone, string> = {
  DEFAULT: 'bg-slate-400',
  PRIMARY: 'bg-[var(--brand)]',
  SECONDARY: 'bg-slate-800',
  TERTIARY: 'bg-amber-500',
}

function truncate(text: string, max: number) {
  return text.length <= max ? text : `${text.slice(0, max - 1)}…`
}

function SpecsGrid({ items, cols }: { items: SpecItem[]; cols: string }) {
  return (
    <div className={`grid gap-2 ${cols}`}>
      {items.map((item) => (
        <div key={`${item.label}:${item.value}`} className="rounded-lg border border-slate-200 bg-slate-50 p-3">
          <span className="block text-[10px] font-semibold tracking-wider text-slate-400 uppercase">{item.label}</span>
          <div className="mt-0.5 text-sm font-semibold text-slate-900">{item.value}</div>
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
          className="inline-flex items-center gap-1.5 rounded-md border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-700"
        >
          <span className={`h-1.5 w-1.5 rounded-full ${TONE_DOT[p.tone ?? 'DEFAULT']}`} aria-hidden />
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
    <div className="grid gap-2 sm:grid-cols-2">
      {items.map((item) => (
        <div key={item.title} className="rounded-lg border border-slate-200 bg-slate-50 p-3">
          <div className="text-sm font-semibold text-slate-900">{item.title}</div>
          {item.body && <p className="mt-1 text-xs leading-relaxed text-slate-500">{item.body}</p>}
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
              className="rounded border border-slate-200 bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700"
            >
              {t}
            </span>
          ))}
        </div>
      )}
    </>
  )
}

function FullSection({ section }: { section: OfferSection }) {
  switch (section.type) {
    case 'SPECS':
      return <SpecsGrid items={section.items} cols="grid-cols-2 sm:grid-cols-4" />
    case 'SOURCE':
      return (
        <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-slate-50 p-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2 text-xs tracking-wider text-slate-400 uppercase">
              Źródło zewnętrzne
              <span className="rounded bg-white px-1.5 py-0.5 font-mono text-[11px] normal-case text-[var(--brand)]">
                {section.engineLabel}
                {section.scraperId ? ` · ${section.scraperId}` : ''}
              </span>
            </div>
            <p className="mt-1 truncate font-mono text-xs text-slate-500">{section.url}</p>
          </div>
          <div className="flex shrink-0 gap-2">
            <button
              type="button"
              className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700"
              onClick={() => void navigator.clipboard.writeText(section.url)}
            >
              Kopiuj link
            </button>
            <a
              className="rounded-lg bg-[var(--brand)] px-3 py-1.5 text-sm text-white hover:bg-[var(--brand-600)]"
              href={section.url}
              target="_blank"
              rel="noreferrer"
            >
              Otwórz stronę
            </a>
          </div>
        </div>
      )
    case 'NARRATIVE':
      return (
        <div className="space-y-3">
          <h3 className="text-lg font-semibold tracking-tight text-slate-900">{section.title}</h3>
          {section.paragraphs.map((p, i) => (
            <p key={i} className="text-sm leading-relaxed whitespace-pre-wrap text-slate-600">
              {p}
            </p>
          ))}
        </div>
      )
    case 'PILLS':
      return (
        <div className="space-y-2">
          <h3 className="text-base font-semibold text-slate-900">{section.title}</h3>
          <PillChips items={section.items} showBadge />
        </div>
      )
    case 'CHECKLIST':
    case 'CARDS':
      return (
        <div className="space-y-2">
          <h3 className="text-base font-semibold text-slate-900">{section.title}</h3>
          <TitledGrid items={section.items} />
        </div>
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
          className="inline-block text-sm font-medium text-[var(--brand)] underline"
          href={section.url}
          target="_blank"
          rel="noreferrer"
        >
          {section.engineLabel}
        </a>
      )
    case 'NARRATIVE':
      return (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">{section.title}</h3>
          <p className="mt-1 text-sm leading-relaxed text-slate-600">
            {truncate(section.paragraphs[0] ?? '', 180)}
          </p>
        </div>
      )
    case 'PILLS':
      return (
        <div className="space-y-2">
          <h3 className="text-sm font-semibold text-slate-900">{section.title}</h3>
          <PillChips items={section.items} />
        </div>
      )
    case 'CHECKLIST':
    case 'CARDS':
      return (
        <p className="text-sm text-slate-500">
          {section.title} · {section.items.length} pozycji
        </p>
      )
  }
}

export function OfferSectionsView({ offer }: { offer: JobOffer }) {
  if (offer.sections.length === 0) return <LegacyBody offer={offer} />
  return (
    <div className="space-y-6">
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
