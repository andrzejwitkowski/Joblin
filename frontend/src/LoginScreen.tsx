import { BrandMark } from './BrandMark'

export function LoginScreen() {
  return (
    <div className="grid min-h-full place-items-center bg-[var(--paper)] px-6">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-[var(--shadow-card)]">
        <div className="mb-6 flex flex-col items-center text-center">
          <BrandMark size={12} />
          <div className="mt-3 text-2xl font-bold tracking-tight text-slate-900">Joblin</div>
          <div className="mt-0.5 text-[10px] font-semibold tracking-wider text-slate-400 uppercase">Recruit OS</div>
        </div>
        <h1 className="mb-2 text-center text-lg font-semibold text-slate-900">Zaloguj się</h1>
        <p className="mb-6 text-center text-sm text-slate-500">Google login. Tylko konta z whitelisty.</p>
        <a
          href="/oauth2/authorization/google"
          className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-[var(--brand)] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-[var(--brand-600)]"
        >
          Continue with Google
        </a>
      </div>
    </div>
  )
}
