import { useEffect, useState } from 'react'
import { getMe, getUsers, type JobOffer, type Me, type SourceBot, type UserSummary } from './api'
import { Board } from './Board'
import { LoginScreen } from './LoginScreen'
import { OfferDetail } from './OfferDetail'
import { OfferDrawer } from './OfferDrawer'
import { Shell } from './Shell'
import { useOffers } from './useOffers'

type Selection = { offer: JobOffer; mode: 'drawer' | 'detail' }

export default function App() {
  const [me, setMe] = useState<Me | null | undefined>(undefined)
  const [users, setUsers] = useState<UserSummary[]>([])
  const [ownerUserId, setOwnerUserId] = useState('')
  const [sourceBot, setSourceBot] = useState<'' | SourceBot>('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [selection, setSelection] = useState<Selection | null>(null)
  const [bootError, setBootError] = useState<string | null>(null)

  const { offers, moveOffer } = useOffers(me, ownerUserId, { sourceBot, from, to })

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const user = await getMe()
        if (cancelled) return
        setMe(user)
        if (user.role === 'ADMIN') {
          try {
            const list = await getUsers()
            if (cancelled) return
            setUsers(list)
            setOwnerUserId((list.find((u) => u.role === 'USER') ?? list[0] ?? user).id)
          } catch (err) {
            console.error(err)
            if (!cancelled) setBootError('Failed to load users')
            setOwnerUserId(user.id)
          }
        } else {
          setOwnerUserId(user.id)
        }
      } catch {
        if (!cancelled) setMe(null)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  if (me === undefined) {
    return <div className="grid min-h-screen place-items-center text-[var(--muted)]">Loading…</div>
  }
  if (me === null) return <LoginScreen />

  const syncOffer = (updated: JobOffer | null) => {
    if (!updated) return
    setSelection((cur) => (cur?.offer.id === updated.id ? { ...cur, offer: updated } : cur))
  }

  return (
    <Shell me={me} users={users} ownerUserId={ownerUserId} onOwnerChange={setOwnerUserId}>
      {bootError && <p className="text-sm text-red-700">{bootError}</p>}

      {selection?.mode === 'detail' ? (
        <OfferDetail
          offer={selection.offer}
          onBack={() => setSelection(null)}
          onStatusChange={(status) =>
            moveOffer(status, selection.offer.id).then(syncOffer).catch(console.error)
          }
        />
      ) : (
        <>
          <div className="flex flex-wrap gap-3 text-sm">
            <label className="flex items-center gap-2">
              Source
              <select
                className="rounded border border-[var(--line)] bg-white px-2 py-1"
                value={sourceBot}
                onChange={(e) => setSourceBot(e.target.value as '' | SourceBot)}
              >
                <option value="">All</option>
                <option value="HERMES">Hermes</option>
                <option value="GROK">Grok</option>
              </select>
            </label>
            <label className="flex items-center gap-2">
              From
              <input
                type="date"
                className="rounded border border-[var(--line)] bg-white px-2 py-1"
                value={from}
                onChange={(e) => setFrom(e.target.value)}
              />
            </label>
            <label className="flex items-center gap-2">
              To
              <input
                type="date"
                className="rounded border border-[var(--line)] bg-white px-2 py-1"
                value={to}
                onChange={(e) => setTo(e.target.value)}
              />
            </label>
          </div>

          <Board
            offers={offers}
            onSelect={(offer) => setSelection({ offer, mode: 'drawer' })}
            onStatusChange={(status, id) =>
              moveOffer(status, id).then(syncOffer).catch(console.error)
            }
          />

          {selection?.mode === 'drawer' && (
            <OfferDrawer
              offer={selection.offer}
              onClose={() => setSelection(null)}
              onOpenFull={() => setSelection({ offer: selection.offer, mode: 'detail' })}
            />
          )}
        </>
      )}
    </Shell>
  )
}
