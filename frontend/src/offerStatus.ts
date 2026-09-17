import type { JobOffer, OfferStatus } from './api'

export const OFFER_STATUSES: OfferStatus[] = ['NEW', 'INTERESTED', 'APPLIED', 'NOT_FOR_ME', 'CLOSED']

export const TERMINAL_STATUSES: OfferStatus[] = ['NOT_FOR_ME', 'CLOSED']

/** Full fade window: 3 days in ms. */
export const FADE_MS = 3 * 24 * 60 * 60 * 1000

export const STATUS_META: Record<
  OfferStatus,
  { label: string; dot: string; badge: string; pill: string }
> = {
  NEW: {
    label: 'Nowe',
    dot: 'bg-blue-500',
    badge: 'bg-blue-100 text-blue-700',
    pill: 'bg-blue-50 text-blue-800 border-blue-200/80',
  },
  INTERESTED: {
    label: 'Zainteresowany',
    dot: 'bg-amber-500',
    badge: 'bg-amber-100 text-amber-800',
    pill: 'bg-amber-50 text-amber-800 border-amber-200/80',
  },
  APPLIED: {
    label: 'Aplikowane',
    dot: 'bg-emerald-500',
    badge: 'bg-emerald-100 text-emerald-800',
    pill: 'bg-emerald-50 text-emerald-800 border-emerald-200/80',
  },
  NOT_FOR_ME: {
    label: 'Odrzucone',
    dot: 'bg-slate-400',
    badge: 'bg-slate-200 text-slate-700',
    pill: 'bg-slate-200 text-slate-600 border-slate-200',
  },
  CLOSED: {
    label: 'Nie wyszło',
    dot: 'bg-rose-400',
    badge: 'bg-rose-100 text-rose-800',
    pill: 'bg-rose-50 text-rose-800 border-rose-200/80',
  },
}

export function isTerminal(status: OfferStatus): boolean {
  return TERMINAL_STATUSES.includes(status)
}

/** Opacity 1 → 0.15 over FADE_MS; 0 when fully elapsed. */
export function fadeOpacity(fadeStartedAt: string | null | undefined, now = Date.now()): number {
  if (!fadeStartedAt) return 1
  const age = now - new Date(fadeStartedAt).getTime()
  if (age <= 0) return 1
  if (age >= FADE_MS) return 0
  return 1 - (age / FADE_MS) * 0.85
}

export function isFadeGone(fadeStartedAt: string | null | undefined, now = Date.now()): boolean {
  if (!fadeStartedAt) return false
  return now - new Date(fadeStartedAt).getTime() >= FADE_MS
}

export function visibleOffers(offers: JobOffer[], now = Date.now()): JobOffer[] {
  return offers.filter((o) => !o.isDeleted && !isFadeGone(o.fadeStartedAt, now))
}

export function groupByStatus(offers: JobOffer[]): Record<OfferStatus, JobOffer[]> {
  const map: Record<OfferStatus, JobOffer[]> = {
    NEW: [],
    INTERESTED: [],
    APPLIED: [],
    NOT_FOR_ME: [],
    CLOSED: [],
  }
  for (const o of offers) map[o.status].push(o)
  return map
}

export function countByStatus(offers: JobOffer[]): Record<OfferStatus, number> {
  const c: Record<OfferStatus, number> = {
    NEW: 0,
    INTERESTED: 0,
    APPLIED: 0,
    NOT_FOR_ME: 0,
    CLOSED: 0,
  }
  for (const o of offers) c[o.status]++
  return c
}

export function matchesSearch(offer: JobOffer, q: string): boolean {
  const needle = q.trim().toLowerCase()
  if (!needle) return true
  return (
    offer.title.toLowerCase().includes(needle) ||
    offer.company.toLowerCase().includes(needle) ||
    offer.tags.some((t) => t.toLowerCase().includes(needle))
  )
}

if (import.meta.env.DEV) {
  const t0 = Date.parse('2026-01-01T00:00:00Z')
  console.assert(fadeOpacity(null) === 1)
  console.assert(Math.abs(fadeOpacity('2026-01-01T00:00:00Z', t0) - 1) < 1e-9)
  console.assert(Math.abs(fadeOpacity('2026-01-01T00:00:00Z', t0 + FADE_MS / 2) - 0.575) < 1e-9)
  console.assert(fadeOpacity('2026-01-01T00:00:00Z', t0 + FADE_MS) === 0)
  console.assert(isFadeGone('2026-01-01T00:00:00Z', t0 + FADE_MS))
  console.assert(!isFadeGone('2026-01-01T00:00:00Z', t0 + FADE_MS - 1))
}
