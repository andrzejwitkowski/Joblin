export type Role = 'USER' | 'ADMIN'
export type OfferStatus = 'NEW' | 'INTERESTED' | 'APPLIED' | 'NOT_FOR_ME'
export type SourceBot = 'HERMES' | 'GROK'
export type OfferTone = 'DEFAULT' | 'PRIMARY' | 'SECONDARY' | 'TERTIARY'

export const OFFER_STATUSES: { value: OfferStatus; label: string }[] = [
  { value: 'NEW', label: 'New' },
  { value: 'INTERESTED', label: 'Interested' },
  { value: 'APPLIED', label: 'Applied' },
  { value: 'NOT_FOR_ME', label: 'Not for me' },
]

export type Me = {
  id: string
  email: string
  displayName: string
  role: Role
}

export type UserSummary = {
  id: string
  email: string
  displayName: string
  role: Role
}

export type SpecItem = {
  label: string
  value: string
  hint?: string | null
  icon?: string | null
}

export type PillItem = {
  label: string
  tone?: OfferTone | null
  badge?: string | null
  icon?: string | null
}

export type TitledItem = {
  title: string
  body?: string | null
  icon?: string | null
}

export type SpecsSection = { type: 'SPECS'; items: SpecItem[]; icon?: string | null }
export type SourceSection = {
  type: 'SOURCE'
  engineLabel: string
  url: string
  scraperId?: string | null
  icon?: string | null
}
export type NarrativeSection = {
  type: 'NARRATIVE'
  title: string
  paragraphs: string[]
  icon?: string | null
}
export type ChecklistSection = {
  type: 'CHECKLIST'
  title: string
  items: TitledItem[]
  icon?: string | null
}
export type PillsSection = {
  type: 'PILLS'
  title: string
  items: PillItem[]
  icon?: string | null
}
export type CardsSection = {
  type: 'CARDS'
  title: string
  items: TitledItem[]
  icon?: string | null
}

export type OfferSection =
  | SpecsSection
  | SourceSection
  | NarrativeSection
  | ChecklistSection
  | PillsSection
  | CardsSection

export type JobOffer = {
  id: string
  ownerUserId: string
  sourceUrl: string
  title: string
  company: string
  description: string
  salary: string | null
  tags: string[]
  sourceBot: SourceBot
  status: OfferStatus
  foundAt: string
  updatedAt: string
  location: string | null
  workMode: string | null
  employmentLabel: string | null
  schemaVersion: number
  sections: OfferSection[]
}

function csrfHeaders(): HeadersInit {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]+)/)
  if (!match) return {}
  return { 'X-XSRF-TOKEN': decodeURIComponent(match[1]) }
}

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...csrfHeaders(),
      ...(init?.headers ?? {}),
    },
    ...init,
  })
  if (res.status === 401) throw new Error('UNAUTHORIZED')
  if (!res.ok) {
    const body = await res.text()
    throw new Error(body || res.statusText)
  }
  return res.json() as Promise<T>
}

export const getMe = () => api<Me>('/api/me')
export const getUsers = () => api<UserSummary[]>('/api/users')
export const getOffers = (params: URLSearchParams) =>
  api<JobOffer[]>(`/api/offers?${params}`)
export const patchOfferStatus = (id: string, status: OfferStatus) =>
  api<JobOffer>(`/api/offers/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
export const logout = () => api<{ ok: string }>('/api/logout', { method: 'POST' })
