import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { normalizeLocale, SUPPORTED_LOCALES, type AppLocale } from './locale.ts'

function assert(cond: unknown, msg: string): asserts cond {
  if (!cond) throw new Error(msg)
}

function leafKeys(obj: unknown, prefix = ''): string[] {
  if (obj === null || typeof obj !== 'object' || Array.isArray(obj)) return prefix ? [prefix] : []
  const out: string[] = []
  for (const [k, v] of Object.entries(obj as Record<string, unknown>)) {
    const path = prefix ? `${prefix}.${k}` : k
    if (v !== null && typeof v === 'object' && !Array.isArray(v)) out.push(...leafKeys(v, path))
    else out.push(path)
  }
  return out.sort()
}

const dir = dirname(fileURLToPath(import.meta.url))
const en = JSON.parse(readFileSync(join(dir, 'locales/en.json'), 'utf8'))
const pl = JSON.parse(readFileSync(join(dir, 'locales/pl.json'), 'utf8'))
const de = JSON.parse(readFileSync(join(dir, 'locales/de.json'), 'utf8'))

const enKeys = leafKeys(en).join('\n')
assert(leafKeys(pl).join('\n') === enKeys, 'pl keys must match en')
assert(leafKeys(de).join('\n') === enKeys, 'de keys must match en')

assert(normalizeLocale('pl-PL') === 'pl', 'normalize pl-PL')
assert(normalizeLocale('de') === 'de', 'normalize de')
assert(normalizeLocale('fr') === 'en', 'unknown → en')
assert(normalizeLocale(undefined) === 'en', 'missing → en')
assert(normalizeLocale('garbage') === 'en', 'garbage → en')
assert((SUPPORTED_LOCALES as readonly AppLocale[]).includes('en'), 'en supported')

console.log('i18n self-check ok')
