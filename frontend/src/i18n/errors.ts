import i18n from './index'

const API_ERROR_KEYS: Record<string, string> = {
  'Not found': 'errors.notFound',
  Forbidden: 'errors.forbidden',
  Unauthorized: 'errors.unauthorized',
  'Invalid request': 'errors.invalidRequest',
  'Invalid request body': 'errors.invalidRequestBody',
  'Failed to load users': 'errors.loadUsers',
  'Failed to update status': 'errors.updateStatus',
}

/** Map a stable English API/app error message to the active locale. */
export function translateApiError(raw: unknown): string {
  const message = raw instanceof Error ? raw.message : typeof raw === 'string' ? raw : ''
  const key = message ? API_ERROR_KEYS[message] : undefined
  return i18n.t(key ?? 'errors.generic')
}
