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

export function translateApiError(raw: unknown): string {
  const message = raw instanceof Error ? raw.message : typeof raw === 'string' ? raw : ''
  if (!message) return i18n.t('errors.generic')
  const key = API_ERROR_KEYS[message]
  return key ? i18n.t(key) : i18n.t('errors.generic')
}
