import i18n from './index'

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString(i18n.language)
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString(i18n.language)
}
