export type Role = 'USER' | 'ADMIN'
export type OfferStatus = 'NEW' | 'INTERESTED' | 'APPLIED' | 'NOT_FOR_ME'
export type SourceBot = 'HERMES' | 'GROK'

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
