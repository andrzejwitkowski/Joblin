import {
  cyclePreference,
  resolveTheme,
  type ThemePreference,
} from './theme.ts'

function assert(cond: unknown, msg: string): asserts cond {
  if (!cond) throw new Error(msg)
}

assert(resolveTheme('light', true) === 'light', 'light ignores system dark')
assert(resolveTheme('dark', false) === 'dark', 'dark ignores system light')
assert(resolveTheme('system', true) === 'dark', 'system + dark mq')
assert(resolveTheme('system', false) === 'light', 'system + light mq')

assert(cyclePreference('light') === 'dark', 'cycle light→dark')
assert(cyclePreference('dark') === 'system', 'cycle dark→system')
assert(cyclePreference('system') === 'light', 'cycle system→light')

const order: ThemePreference[] = ['light', 'dark', 'system']
for (const p of order) {
  assert(order.includes(cyclePreference(p)), `cycle stays in set from ${p}`)
}

console.log('theme self-check ok')
