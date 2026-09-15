import type { JobOffer } from './api'

export function OfferDrawer({ offer, onClose }: { offer: JobOffer; onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-20 flex justify-end bg-black/30" onClick={onClose}>
      <aside
        className="h-full w-full max-w-md overflow-y-auto border-l border-[var(--line)] bg-[var(--paper)] p-5 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <button type="button" className="mb-4 text-sm text-[var(--muted)]" onClick={onClose}>
          Close
        </button>
        <h2 className="text-2xl font-semibold">{offer.title}</h2>
        <p className="mt-1 text-[var(--muted)]">{offer.company}</p>
        {offer.salary && <p className="mt-3 text-sm">Salary: {offer.salary}</p>}
        <a
          className="mt-3 inline-block text-sm text-[var(--accent)] underline"
          href={offer.sourceUrl}
          target="_blank"
          rel="noreferrer"
        >
          Open source
        </a>
        <p className="mt-4 whitespace-pre-wrap text-sm leading-relaxed">{offer.description}</p>
        {offer.tags.length > 0 && (
          <div className="mt-4 flex flex-wrap gap-2">
            {offer.tags.map((t) => (
              <span key={t} className="rounded bg-[var(--panel)] px-2 py-0.5 text-xs">
                {t}
              </span>
            ))}
          </div>
        )}
        <dl className="mt-6 space-y-1 text-xs text-[var(--muted)]">
          <div>Status: {offer.status}</div>
          <div>Source: {offer.sourceBot}</div>
          <div>Found: {new Date(offer.foundAt).toLocaleString()}</div>
          <div>Updated: {new Date(offer.updatedAt).toLocaleString()}</div>
        </dl>
      </aside>
    </div>
  )
}
