import { useCallback, useEffect, useState } from 'react'
import { getOffers, patchOfferStatus, type JobOffer, type Me, type OfferStatus, type SourceBot } from './api'

export type OfferFilters = {
  sourceBot: '' | SourceBot
  from: string
  to: string
}

export function useOffers(me: Me | null | undefined, ownerUserId: string, filters: OfferFilters) {
  const [offers, setOffers] = useState<JobOffer[]>([])

  const reload = useCallback(async () => {
    if (!me || !ownerUserId) return
    const params = new URLSearchParams()
    if (me.role === 'ADMIN') params.set('ownerUserId', ownerUserId)
    if (filters.sourceBot) params.set('sourceBot', filters.sourceBot)
    if (filters.from) params.set('from', new Date(filters.from).toISOString())
    if (filters.to) params.set('to', new Date(`${filters.to}T23:59:59Z`).toISOString())
    setOffers(await getOffers(params))
  }, [me, ownerUserId, filters.sourceBot, filters.from, filters.to])

  useEffect(() => {
    reload().catch(console.error)
  }, [reload])

  async function moveOffer(status: OfferStatus, offerId: string) {
    const offer = offers.find((o) => o.id === offerId)
    if (!offer || offer.status === status) return null
    const updated = await patchOfferStatus(offerId, status)
    setOffers((prev) => prev.map((o) => (o.id === offerId ? updated : o)))
    return updated
  }

  return { offers, moveOffer }
}
