export const SUPPORTED_LOCALES = ['en', 'pl', 'de'] as const
export type AppLocale = (typeof SUPPORTED_LOCALES)[number]

export const LOCALE_STORAGE_KEY = 'joblin-locale'

export function isAppLocale(value: string | undefined | null): value is AppLocale {
  return value === 'en' || value === 'pl' || value === 'de'
}

export function normalizeLocale(raw: string | undefined | null): AppLocale {
  if (!raw) return 'en'
  const base = raw.toLowerCase().split('-')[0]
  return isAppLocale(base) ? base : 'en'
}
