import { useEffect, useState } from 'react'
import { getMe, getUsers, type JobOffer, type Me, type OfferStatus, type SourceBot, type UserSummary } from './api'
import { Board } from './Board'
import { LoginScreen } from './LoginScreen'
import { OfferDetail } from './OfferDetail'
import { OfferDrawer } from './OfferDrawer'
import { OfferList } from './OfferList'
import { matchesSearch } from './offerStatus'
import { OffersToolbar, type ViewMode } from './OffersToolbar'
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
  const [search, setSearch] = useState('')
  const [view, setView] = useState<ViewMode>('kanban')
  const [selection, setSelection] = useState<Selection | null>(null)
  const [bootError, setBootError] = useState<string | null>(null)
  const [statusError, setStatusError] = useState<string | null>(null)

  const { offers, moveOffer } = useOffers(me, ownerUserId, { sourceBot, from, to })
  const filtered = offers.filter((o) => matchesSearch(o, search))

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

  function syncOffer(updated: JobOffer | null) {
    if (!updated) return
    setSelection((cur) => (cur?.offer.id === updated.id ? { ...cur, offer: updated } : cur))
  }

  function handleStatusChange(status: OfferStatus, id: string) {
    setStatusError(null)
    moveOffer(status, id)
      .then(syncOffer)
      .catch((err: unknown) => {
        console.error(err)
        setStatusError(err instanceof Error ? err.message : 'Failed to update status')
      })
  }

  if (me === undefined) {
    return <div className="grid min-h-full place-items-center text-slate-500">Ładowanie…</div>
  }
  if (me === null) return <LoginScreen />

  if (selection?.mode === 'detail') {
    return (
      <Shell
        me={me}
        users={users}
        ownerUserId={ownerUserId}
        onOwnerChange={setOwnerUserId}
        search={search}
        onSearchChange={setSearch}
      >
        {statusError && <p className="px-5 pt-2 text-sm text-red-700">{statusError}</p>}
        <OfferDetail
          offer={selection.offer}
          onBack={() => setSelection(null)}
          onStatusChange={(status) => handleStatusChange(status, selection.offer.id)}
        />
      </Shell>
    )
  }

  return (
    <Shell
      me={me}
      users={users}
      ownerUserId={ownerUserId}
      onOwnerChange={setOwnerUserId}
      search={search}
      onSearchChange={setSearch}
    >
      {bootError && <p className="px-5 pt-2 text-sm text-red-700">{bootError}</p>}
      {statusError && <p className="px-5 pt-2 text-sm text-red-700">{statusError}</p>}

      <OffersToolbar
        view={view}
        onViewChange={setView}
        count={filtered.length}
        sourceBot={sourceBot}
        from={from}
        to={to}
        hasFilters={Boolean(sourceBot || from || to || search.trim())}
        onSourceChange={setSourceBot}
        onFromChange={setFrom}
        onToChange={setTo}
        onClearFilters={() => {
          setSourceBot('')
          setFrom('')
          setTo('')
          setSearch('')
        }}
      />

      {view === 'kanban' ? (
        <Board
          offers={filtered}
          onSelect={(offer) => setSelection({ offer, mode: 'drawer' })}
          onStatusChange={handleStatusChange}
        />
      ) : (
        <OfferList
          offers={filtered}
          onSelect={(offer) => setSelection({ offer, mode: 'drawer' })}
          onStatusChange={handleStatusChange}
        />
      )}

      {selection?.mode === 'drawer' && (
        <OfferDrawer
          offer={selection.offer}
          onClose={() => setSelection(null)}
          onOpenFull={() => setSelection({ offer: selection.offer, mode: 'detail' })}
        />
      )}
    </Shell>
  )
}
