export function BrandMark({ size = 8 }: { size?: 8 | 12 }) {
  const box = size === 12 ? 'h-12 w-12' : 'h-8 w-8'
  const icon = size === 12 ? 'h-6 w-6' : 'h-5 w-5'
  return (
    <div
      className={`flex ${box} items-center justify-center rounded-lg bg-gradient-to-tr from-[var(--brand-600)] to-blue-500 text-white shadow-sm shadow-blue-500/30`}
    >
      <svg className={icon} fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden>
        <path
          d="M20 7h-4V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2zM10 5h4v2h-4V5zm10 14H4v-7h16v7zm0-9H4V9h16v1z"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="1.8"
        />
      </svg>
    </div>
  )
}
