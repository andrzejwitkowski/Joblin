import { useEffect, useRef, type ReactNode } from 'react'
import { LogOut, Search } from 'lucide-react'
import { logout, type Me, type UserSummary } from './api'
import { BrandMark } from './BrandMark'

export function Shell({
  children,
  me,
  users,
  ownerUserId,
  onOwnerChange,
  search,
  onSearchChange,
}: {
  children: ReactNode
  me: Me
  users: UserSummary[]
  ownerUserId: string
  onOwnerChange: (id: string) => void
  search: string
  onSearchChange: (q: string) => void
}) {
  const searchRef = useRef<HTMLInputElement>(null)
  const initial = (me.displayName.trim()[0] ?? '?').toUpperCase()

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault()
        searchRef.current?.focus()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  return (
    <div className="flex h-full flex-col overflow-hidden bg-[var(--paper)]">
      <header className="z-30 flex-shrink-0 border-b border-slate-200 bg-white">
        <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-2.5">
          <div className="flex items-center gap-4 sm:gap-6">
            <a className="flex items-center gap-2.5" href="/">
              <BrandMark />
              <div className="flex flex-col">
                <span className="text-lg leading-tight font-bold tracking-tight text-slate-900">Joblin</span>
                <span className="text-[10px] font-semibold tracking-wider text-slate-400 uppercase">Recruit OS</span>
              </div>
            </a>

            {me.role === 'ADMIN' && (
              <>
                <div className="hidden h-6 w-px bg-slate-200 sm:block" />
                <div className="flex items-center gap-2">
                  <span className="hidden text-xs font-medium text-slate-400 lg:inline">Kandydat:</span>
                  <select
                    className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-800 focus:ring-2 focus:ring-[var(--brand)] focus:outline-none"
                    value={ownerUserId}
                    onChange={(e) => onOwnerChange(e.target.value)}
                    aria-label="Wybierz kandydata"
                  >
                    {users.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.displayName} ({u.role})
                      </option>
                    ))}
                  </select>
                </div>
              </>
            )}
          </div>

          <div className="order-last w-full max-w-md flex-1 sm:order-none sm:mx-4">
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                <Search className="h-4 w-4 text-slate-400" aria-hidden />
              </div>
              <input
                ref={searchRef}
                className="w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pr-12 pl-9 text-xs placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-[var(--brand)] focus:outline-none"
                placeholder="Szukaj ofert, firm, technologii..."
                type="search"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
              />
              <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5 font-mono text-[10px] text-slate-400">
                ⌘K
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-slate-800 text-xs font-semibold text-white">
              {initial}
            </div>
            <div className="hidden flex-col text-left leading-tight sm:flex">
              <span className="text-xs font-semibold text-slate-800">{me.displayName}</span>
              <span className="text-[10px] font-medium text-[var(--brand)]">{me.role}</span>
            </div>
            <button
              type="button"
              className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
              title="Wyloguj się"
              onClick={() => logout().then(() => window.location.assign('/'))}
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </header>

      <div className="flex min-h-0 flex-1 flex-col overflow-hidden">{children}</div>
    </div>
  )
}
