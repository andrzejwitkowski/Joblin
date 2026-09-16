import type { JobOffer, OfferStatus } from './api'

export const OFFER_STATUSES: OfferStatus[] = ['NEW', 'INTERESTED', 'APPLIED', 'NOT_FOR_ME']

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
}

export function groupByStatus(offers: JobOffer[]): Record<OfferStatus, JobOffer[]> {
  const map: Record<OfferStatus, JobOffer[]> = { NEW: [], INTERESTED: [], APPLIED: [], NOT_FOR_ME: [] }
  for (const o of offers) map[o.status].push(o)
  return map
}

export function countByStatus(offers: JobOffer[]): Record<OfferStatus, number> {
  const c: Record<OfferStatus, number> = { NEW: 0, INTERESTED: 0, APPLIED: 0, NOT_FOR_ME: 0 }
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
