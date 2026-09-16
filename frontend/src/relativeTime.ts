export function relativeTime(iso: string, now = Date.now()): string {
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) return ''
  const diffSec = Math.round((now - then) / 1000)
  if (diffSec < 60) return 'przed chwilą'
  const diffMin = Math.round(diffSec / 60)
  if (diffMin < 60) return `${diffMin} min temu`
  const diffH = Math.round(diffMin / 60)
  if (diffH < 24) return `${diffH} godz. temu`
  const startToday = new Date(now)
  startToday.setHours(0, 0, 0, 0)
  const startThen = new Date(then)
  startThen.setHours(0, 0, 0, 0)
  const dayDiff = Math.round((startToday.getTime() - startThen.getTime()) / 86_400_000)
  if (dayDiff === 1) return 'Wczoraj'
  if (dayDiff < 7) return `${dayDiff} dni temu`
  return new Date(iso).toLocaleDateString('pl-PL')
}

// ponytail: tiny self-check; upgrade to a real test if this grows.
if (import.meta.env.DEV) {
  const t0 = Date.parse('2024-10-15T12:00:00Z')
  console.assert(relativeTime('2024-10-15T11:46:00Z', t0) === '14 min temu')
  console.assert(relativeTime('2024-10-14T12:00:00Z', t0) === 'Wczoraj')
}
