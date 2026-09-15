import { LogOut } from 'lucide-react'
import type { ReactNode } from 'react'
import { logout, type Me, type UserSummary } from './api'

export function Shell({
  children,
  me,
  users,
  ownerUserId,
  onOwnerChange,
}: {
  children: ReactNode
  me: Me
  users: UserSummary[]
  ownerUserId: string
  onOwnerChange: (id: string) => void
}) {
  return (
    <div className="mx-auto flex min-h-screen max-w-[1400px] flex-col gap-4 px-4 py-4 md:px-6">
      <header className="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--line)] pb-4">
        <div>
          <div className="text-xs uppercase tracking-[0.18em] text-[var(--muted)]">Joblin</div>
          <h1 className="text-2xl font-semibold tracking-tight">Offer board</h1>
        </div>
        <div className="flex flex-wrap items-center gap-3 text-sm">
          <span className="text-[var(--muted)]">{me.displayName}</span>
          {me.role === 'ADMIN' && (
            <select
              className="rounded border border-[var(--line)] bg-white px-2 py-1.5"
              value={ownerUserId}
              onChange={(e) => onOwnerChange(e.target.value)}
            >
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.displayName} ({u.role})
                </option>
              ))}
            </select>
          )}
          <button
            type="button"
            className="inline-flex items-center gap-1 rounded border border-[var(--line)] px-2 py-1.5"
            onClick={() => logout().then(() => window.location.assign('/'))}
          >
            <LogOut size={16} /> Logout
          </button>
        </div>
      </header>
      {children}
    </div>
  )
}
