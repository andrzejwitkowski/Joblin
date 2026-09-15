export function LoginScreen() {
  return (
    <div className="grid min-h-screen place-items-center px-6">
      <div className="w-full max-w-md text-center">
        <div className="mb-2 text-sm uppercase tracking-[0.2em] text-[var(--muted)]">Joblin</div>
        <h1 className="mb-3 text-4xl font-semibold tracking-tight">Sign in to triage offers</h1>
        <p className="mb-8 text-[var(--muted)]">Google login. Seeded accounts only.</p>
        <a
          href="/oauth2/authorization/google"
          className="inline-flex items-center gap-2 rounded-md bg-[var(--accent)] px-5 py-3 text-white"
        >
          Continue with Google
        </a>
      </div>
    </div>
  )
}
