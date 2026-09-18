# Dark / light theming — design

## Goal

Add light/dark theming to the Joblin SPA with OS-aware default and a manual override.

## Decisions

- Preference: `light | dark | system` (default `system`), stored in `localStorage` under `joblin-theme`
- Control: three-way cycle (Light → Dark → System) in the Shell header only
- Mechanism: `class="dark"` on `<html>` plus semantic CSS variables (no new dependencies)
- Status chips keep colored Tailwind classes; adjust dark contrast only where needed
- Preference is device-local (no server sync)
- Login screen has no toggle; it still follows boot script + tokens

## Architecture

1. **FOUC boot** — inline script in `index.html` reads storage, resolves theme, toggles `dark` on `<html>` before paint
2. **`theme.ts`** — preference helpers, resolve/apply, `cyclePreference`, `useTheme` hook (subscribes to `prefers-color-scheme` when preference is `system`)
3. **Tokens** — `:root` / `.dark` define `--ink`, `--ink-muted`, `--paper`, `--surface`, `--surface-muted`, `--border`, brand, shadows, scrollbar; `color-scheme` set per theme
4. **UI** — components use token utilities instead of light-only `bg-white` / `slate-*` surfaces

## Edge cases

- Missing or invalid storage → `system`
- OS theme change while preference is `system` → re-apply without reload
- Icon reflects *resolved* theme; label/title shows preference including System

## Out of scope

Settings page, server-synced preference, login-screen toggle, layout/brand redesign
