import { useEffect, useRef, type ReactNode } from 'react'
import { LogOut, Moon, Search, Sun } from 'lucide-react'
import { logout, type Me, type UserSummary } from './api'
import { BrandMark } from './BrandMark'
import { useTheme, type ThemePreference } from './theme'

const PREF_LABEL: Record<ThemePreference, string> = {
  light: 'Jasny',
  dark: 'Ciemny',
  system: 'Systemowy',
}

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
  const { preference, resolved, cycle } = useTheme()
  const themeLabel = `Motyw: ${PREF_LABEL[preference]}`

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
    <div className="flex h-full flex-col overflow-hidden bg-paper">
      <header className="z-30 flex-shrink-0 border-b border-border bg-surface">
        <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-2.5">
          <div className="flex items-center gap-4 sm:gap-6">
            <a className="flex items-center gap-2.5" href="/">
              <BrandMark />
              <div className="flex flex-col">
                <span className="text-lg leading-tight font-bold tracking-tight text-ink">Joblin</span>
                <span className="text-[10px] font-semibold tracking-wider text-ink-muted uppercase">
                  Recruit OS
                </span>
              </div>
            </a>

            {me.role === 'ADMIN' && (
              <>
                <div className="hidden h-6 w-px bg-border sm:block" />
                <div className="flex items-center gap-2">
                  <span className="hidden text-xs font-medium text-ink-muted lg:inline">Kandydat:</span>
                  <select
                    className="rounded-lg border border-border bg-surface-muted px-3 py-1.5 text-xs font-semibold text-ink focus:ring-2 focus:ring-brand focus:outline-none"
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
                <Search className="h-4 w-4 text-ink-muted" aria-hidden />
              </div>
              <input
                ref={searchRef}
                className="w-full rounded-lg border border-border bg-surface-muted py-1.5 pr-12 pl-9 text-xs text-ink placeholder:text-ink-muted focus:bg-surface focus:ring-2 focus:ring-brand focus:outline-none"
                placeholder="Szukaj ofert, firm, technologii..."
                type="search"
                aria-label="Szukaj ofert"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
              />
              <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5 font-mono text-[10px] text-ink-muted">
                ⌘K
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-ink text-xs font-semibold text-paper">
              {initial}
            </div>
            <div className="hidden flex-col text-left leading-tight sm:flex">
              <span className="text-xs font-semibold text-ink">{me.displayName}</span>
              <span className="text-[10px] font-medium text-brand">{me.role}</span>
            </div>
            <button
              type="button"
              className="rounded-lg p-1.5 text-ink-muted hover:bg-surface-muted hover:text-ink"
              title={themeLabel}
              aria-label={themeLabel}
              onClick={cycle}
            >
              {resolved === 'dark' ? <Moon className="h-4 w-4" /> : <Sun className="h-4 w-4" />}
            </button>
            <button
              type="button"
              className="rounded-lg p-1.5 text-ink-muted hover:bg-rose-50 hover:text-rose-600 dark:hover:bg-rose-950/40"
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
