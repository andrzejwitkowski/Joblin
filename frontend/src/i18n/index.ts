import i18n from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import { initReactI18next } from 'react-i18next'
import { LOCALE_STORAGE_KEY, normalizeLocale, SUPPORTED_LOCALES, type AppLocale } from './locale'
import de from './locales/de.json'
import en from './locales/en.json'
import pl from './locales/pl.json'

export type { AppLocale } from './locale'
export { LOCALE_STORAGE_KEY, normalizeLocale, SUPPORTED_LOCALES, isAppLocale } from './locale'

function syncDocumentLang(lng: string) {
  const locale = normalizeLocale(lng)
  document.documentElement.lang = locale
  document.title = i18n.t('app.documentTitle', { lng: locale })
}

i18n.on('languageChanged', syncDocumentLang)

void i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      pl: { translation: pl },
      de: { translation: de },
    },
    supportedLngs: [...SUPPORTED_LOCALES],
    fallbackLng: 'en',
    nonExplicitSupportedLngs: true,
    load: 'languageOnly',
    interpolation: { escapeValue: false },
    detection: {
      order: ['localStorage', 'navigator'],
      lookupLocalStorage: LOCALE_STORAGE_KEY,
      caches: ['localStorage'],
      convertDetectedLanguage: (lng) => normalizeLocale(lng),
    },
  })
  .then(() => syncDocumentLang(i18n.language))

export function setAppLocale(locale: AppLocale) {
  return i18n.changeLanguage(locale)
}

export default i18n
