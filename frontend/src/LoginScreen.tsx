import { BrandMark } from './BrandMark'
import { useTranslation } from 'react-i18next'

export function LoginScreen() {
  const { t } = useTranslation()
  return (
    <div className="grid min-h-full place-items-center bg-paper px-6">
      <div className="w-full max-w-md rounded-2xl border border-border bg-surface p-8 shadow-[var(--shadow-card)]">
        <div className="mb-6 flex flex-col items-center text-center">
          <BrandMark size={12} />
          <div className="mt-3 text-2xl font-bold tracking-tight text-ink">Joblin</div>
          <div className="mt-0.5 text-[10px] font-semibold tracking-wider text-ink-muted uppercase">
            Recruit OS
          </div>
        </div>
        <h1 className="mb-2 text-center text-lg font-semibold text-ink">{t('login.title')}</h1>
        <p className="mb-6 text-center text-sm text-ink-muted">{t('login.subtitle')}</p>
        <a
          href="/oauth2/authorization/google"
          className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-brand px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-brand-600"
        >
          {t('login.continueGoogle')}
        </a>
      </div>
    </div>
  )
}
