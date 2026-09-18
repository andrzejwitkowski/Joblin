import i18n from './i18n'
import { formatDate } from './i18n/format'

export function relativeTime(iso: string, now = Date.now()): string {
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) return ''
  const diffSec = Math.round((now - then) / 1000)
  if (diffSec < 60) return i18n.t('time.justNow')
  const diffMin = Math.round(diffSec / 60)
  if (diffMin < 60) return i18n.t('time.minAgo', { count: diffMin })
  const diffH = Math.round(diffMin / 60)
  if (diffH < 24) return i18n.t('time.hourAgo', { count: diffH })
  const startToday = new Date(now)
  startToday.setHours(0, 0, 0, 0)
  const startThen = new Date(then)
  startThen.setHours(0, 0, 0, 0)
  const dayDiff = Math.round((startToday.getTime() - startThen.getTime()) / 86_400_000)
  if (dayDiff === 1) return i18n.t('time.yesterday')
  if (dayDiff < 7) return i18n.t('time.daysAgo', { count: dayDiff })
  return formatDate(iso)
}

// ponytail: tiny self-check; upgrade to a real test if this grows.
if (import.meta.env.DEV) {
  const t0 = Date.parse('2024-10-15T12:00:00Z')
  console.assert(i18n.t('time.minAgo', { lng: 'en', count: 14 }) === '14 min ago')
  console.assert(i18n.t('time.yesterday', { lng: 'en' }) === 'Yesterday')
  console.assert(relativeTime('2024-10-15T11:59:30Z', t0) === i18n.t('time.justNow'))
}
