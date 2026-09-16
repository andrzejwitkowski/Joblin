import type { JobOffer, OfferSection, OfferTone, PillItem, SpecItem, TitledItem } from './api'

const TONE_DOT: Record<OfferTone, string> = {
  DEFAULT: 'bg-[var(--muted)]',
  PRIMARY: 'bg-[var(--accent)]',
  SECONDARY: 'bg-[var(--ink)]',
  TERTIARY: 'bg-[var(--danger)]',
}

function truncate(text: string, max: number) {
  return text.length <= max ? text : `${text.slice(0, max - 1)}…`
}

function SpecsGrid({ items, cols }: { items: SpecItem[]; cols: string }) {
  return (
    <div className={`grid gap-2 ${cols}`}>
      {items.map((item) => (
        <div key={`${item.label}:${item.value}`} className="rounded-lg border border-[var(--line)] bg-[var(--panel)]/60 p-3">
          <span className="block text-[10px] font-semibold uppercase tracking-wider text-[var(--muted)]">{item.label}</span>
          <div className="mt-0.5 text-sm font-semibold">{item.value}</div>
          {item.hint && <span className="mt-0.5 block text-xs text-[var(--muted)]">{item.hint}</span>}
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
          className="inline-flex items-center gap-1.5 rounded-md border border-[var(--line)] bg-white px-2.5 py-1 text-xs"
        >
          <span className={`h-1.5 w-1.5 rounded-full ${TONE_DOT[p.tone ?? 'DEFAULT']}`} aria-hidden />
          <span className={p.tone === 'PRIMARY' ? 'font-semibold' : undefined}>{p.label}</span>
          {showBadge && p.badge && (
            <span className="rounded bg-[var(--panel)] px-1 py-0.5 text-[10px] text-[var(--accent)]">{p.badge}</span>
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
        <div key={item.title} className="rounded-lg border border-[var(--line)] bg-[var(--panel)]/40 p-3">
          <div className="text-sm font-semibold">{item.title}</div>
          {item.body && <p className="mt-1 text-xs leading-relaxed text-[var(--muted)]">{item.body}</p>}
        </div>
      ))}
    </div>
  )
}

function LegacyBody({ offer }: { offer: JobOffer }) {
  return (
    <>
      <p className="whitespace-pre-wrap text-sm leading-relaxed text-[var(--muted)]">{offer.description}</p>
      {offer.tags.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {offer.tags.map((t) => (
            <span key={t} className="rounded bg-[var(--panel)] px-2 py-0.5 text-xs">
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
        <div className="flex flex-col gap-3 rounded-xl border border-[var(--line)] bg-[var(--panel)]/50 p-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2 text-xs uppercase tracking-wider text-[var(--muted)]">
              External source
              <span className="rounded bg-white px-1.5 py-0.5 font-mono text-[11px] normal-case text-[var(--accent)]">
                {section.engineLabel}
                {section.scraperId ? ` · ${section.scraperId}` : ''}
              </span>
            </div>
            <p className="mt-1 truncate font-mono text-xs text-[var(--muted)]">{section.url}</p>
          </div>
          <div className="flex shrink-0 gap-2">
            <button
              type="button"
              className="rounded-lg border border-[var(--line)] bg-white px-3 py-1.5 text-sm"
              onClick={() => void navigator.clipboard.writeText(section.url)}
            >
              Copy link
            </button>
            <a
              className="rounded-lg bg-[var(--accent)] px-3 py-1.5 text-sm text-white"
              href={section.url}
              target="_blank"
              rel="noreferrer"
            >
              Open page
            </a>
          </div>
        </div>
      )
    case 'NARRATIVE':
      return (
        <div className="space-y-3">
          <h3 className="text-lg font-semibold tracking-tight">{section.title}</h3>
          {section.paragraphs.map((p, i) => (
            <p key={i} className="whitespace-pre-wrap text-sm leading-relaxed text-[var(--muted)]">
              {p}
            </p>
          ))}
        </div>
      )
    case 'PILLS':
      return (
        <div className="space-y-2">
          <h3 className="text-base font-semibold">{section.title}</h3>
          <PillChips items={section.items} showBadge />
        </div>
      )
    case 'CHECKLIST':
    case 'CARDS':
      return (
        <div className="space-y-2">
          <h3 className="text-base font-semibold">{section.title}</h3>
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
        <a className="inline-block text-sm text-[var(--accent)] underline" href={section.url} target="_blank" rel="noreferrer">
          {section.engineLabel}
        </a>
      )
    case 'NARRATIVE':
      return (
        <div>
          <h3 className="text-sm font-semibold">{section.title}</h3>
          <p className="mt-1 text-sm leading-relaxed text-[var(--muted)]">
            {truncate(section.paragraphs[0] ?? '', 180)}
          </p>
        </div>
      )
    case 'PILLS':
      return (
        <div className="space-y-2">
          <h3 className="text-sm font-semibold">{section.title}</h3>
          <PillChips items={section.items} />
        </div>
      )
    case 'CHECKLIST':
    case 'CARDS':
      return (
        <p className="text-sm text-[var(--muted)]">
          {section.title} · {section.items.length} items
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
