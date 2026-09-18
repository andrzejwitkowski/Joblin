import { useEffect, useState, useSyncExternalStore } from 'react'

export type ThemePreference = 'light' | 'dark' | 'system'
export type ResolvedTheme = 'light' | 'dark'

export const THEME_STORAGE_KEY = 'joblin-theme'

const PREFS: ThemePreference[] = ['light', 'dark', 'system']

export function getStoredPreference(): ThemePreference {
  try {
    const raw = localStorage.getItem(THEME_STORAGE_KEY)
    if (raw === 'light' || raw === 'dark' || raw === 'system') return raw
  } catch {
    // private mode / blocked storage
  }
  return 'system'
}

export function setPreference(pref: ThemePreference): void {
  try {
    localStorage.setItem(THEME_STORAGE_KEY, pref)
  } catch {
    // private mode / blocked storage
  }
}

export function systemPrefersDark(): boolean {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export function resolveTheme(pref: ThemePreference, prefersDark = systemPrefersDark()): ResolvedTheme {
  if (pref === 'system') return prefersDark ? 'dark' : 'light'
  return pref
}

export function applyResolvedTheme(resolved: ResolvedTheme): void {
  const root = document.documentElement
  root.classList.toggle('dark', resolved === 'dark')
  root.style.colorScheme = resolved
}

export function cyclePreference(current: ThemePreference): ThemePreference {
  return PREFS[(PREFS.indexOf(current) + 1) % PREFS.length]!
}

function subscribeSystem(cb: () => void) {
  const mq = window.matchMedia('(prefers-color-scheme: dark)')
  mq.addEventListener('change', cb)
  return () => mq.removeEventListener('change', cb)
}

export function useTheme() {
  const [preference, setPrefState] = useState<ThemePreference>(getStoredPreference)
  const systemDark = useSyncExternalStore(subscribeSystem, systemPrefersDark, () => false)
  const resolved = resolveTheme(preference, systemDark)

  useEffect(() => {
    applyResolvedTheme(resolved)
  }, [resolved])

  function cycle() {
    const next = cyclePreference(preference)
    setPreference(next)
    setPrefState(next)
  }

  return { preference, resolved, cycle }
}
