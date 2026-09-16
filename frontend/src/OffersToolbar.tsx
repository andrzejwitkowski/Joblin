import { LayoutGrid, List } from 'lucide-react'
import type { SourceBot } from './api'

export type ViewMode = 'kanban' | 'list'

export function OffersToolbar({
  view,
  onViewChange,
  count,
  sourceBot,
  from,
  to,
  hasFilters,
  onSourceChange,
  onFromChange,
  onToChange,
  onClearFilters,
}: {
  view: ViewMode
  onViewChange: (v: ViewMode) => void
  count: number
  sourceBot: '' | SourceBot
  from: string
  to: string
  hasFilters: boolean
  onSourceChange: (v: '' | SourceBot) => void
  onFromChange: (v: string) => void
  onToChange: (v: string) => void
  onClearFilters: () => void
}) {
  return (
    <section className="flex-shrink-0 border-b border-slate-200 bg-white px-5 py-3">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="flex items-center gap-2">
          <h1 className="text-xl font-bold tracking-tight text-slate-900">
            {view === 'kanban' ? 'Tablica Ofert' : 'Lista Ofert'}
          </h1>
          <span className="rounded-full border border-blue-200 bg-blue-50 px-2 py-0.5 text-xs font-medium text-[var(--brand)]">
            {count}
          </span>
        </div>
        <div className="inline-flex rounded-lg border border-slate-200 bg-slate-100 p-0.5">
          {(
            [
              ['kanban', LayoutGrid, 'Kanban'],
              ['list', List, 'Lista'],
            ] as const
          ).map(([mode, Icon, label]) => (
            <button
              key={mode}
              type="button"
              className={`inline-flex items-center gap-1.5 rounded-md px-3 py-1 text-xs ${
                view === mode
                  ? 'bg-white font-semibold text-slate-800 shadow-sm'
                  : 'font-medium text-slate-500 hover:text-slate-800'
              }`}
              onClick={() => onViewChange(mode)}
            >
              <Icon className="h-3.5 w-3.5" aria-hidden />
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="mt-3 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-3 text-xs">
        <div className="flex flex-wrap items-center gap-2.5">
          <label className="flex items-center gap-1.5 font-medium text-slate-500">
            Źródło:
            <select
              className="rounded-md border border-slate-300 bg-white py-1 pr-8 pl-2.5 font-medium text-slate-700 focus:ring-1 focus:ring-[var(--brand)] focus:outline-none"
              value={sourceBot}
              onChange={(e) => onSourceChange(e.target.value as '' | SourceBot)}
            >
              <option value="">Wszystkie</option>
              <option value="HERMES">Hermes</option>
              <option value="GROK">Grok</option>
            </select>
          </label>
          <label className="flex items-center gap-1.5 font-medium text-slate-500">
            Od:
            <input
              type="date"
              className="w-36 rounded-md border border-slate-300 bg-white py-1 px-2.5 text-slate-700 focus:ring-1 focus:ring-[var(--brand)] focus:outline-none"
              value={from}
              onChange={(e) => onFromChange(e.target.value)}
            />
          </label>
          <label className="flex items-center gap-1.5 font-medium text-slate-500">
            Do:
            <input
              type="date"
              className="w-36 rounded-md border border-slate-300 bg-white py-1 px-2.5 text-slate-700 focus:ring-1 focus:ring-[var(--brand)] focus:outline-none"
              value={to}
              onChange={(e) => onToChange(e.target.value)}
            />
          </label>
        </div>
        {hasFilters && (
          <button type="button" className="font-medium text-slate-500 hover:text-slate-800" onClick={onClearFilters}>
            Wyczyść filtry
          </button>
        )}
      </div>
    </section>
  )
}
