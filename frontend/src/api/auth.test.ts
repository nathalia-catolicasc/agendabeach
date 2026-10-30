import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { LoginResponse } from './types'

const session: LoginResponse = {
  id: 1,
  name: 'Test client',
  email: 'client@example.invalid',
  profile: 'CLIENT',
  accessToken: 'test-access-token',
  tokenType: 'Bearer',
  expiresAt: '2099-01-01T12:00:00',
}

let values: Map<string, string>
let browser: EventTarget & {
  localStorage: {
    getItem: (key: string) => string | null
    setItem: (key: string, value: string) => void
    removeItem: (key: string) => void
  }
}

beforeEach(() => {
  vi.resetModules()
  values = new Map()
  browser = Object.assign(new EventTarget(), {
    localStorage: {
      getItem: (key: string) => values.get(key) ?? null,
      setItem: (key: string, value: string) => {
        values.set(key, value)
      },
      removeItem: (key: string) => {
        values.delete(key)
      },
    },
  })
  vi.stubGlobal('window', browser)
})

afterEach(() => vi.unstubAllGlobals())

function mockResponse(body: unknown, status = 200) {
  const fetch = vi.fn().mockImplementation(async () => new Response(JSON.stringify(body), { status }))
  vi.stubGlobal('fetch', fetch)
  return fetch
}

describe('registration and authentication contracts', () => {
  it('sends CPF in registration without an old credential', async () => {
    const { storeSession } = await import('../session')
    storeSession(session)
    const fetch = mockResponse({ id: 2 })
    const { register } = await import('./index')
    await register('Client', 'new@example.invalid', 'password123', '52998224725')
    const [url, options] = fetch.mock.calls[0]
    expect(url).toBe('/users')
    expect(JSON.parse(options.body)).toMatchObject({ cpf: '52998224725' })
    expect(options.headers.Authorization).toBeUndefined()
  })

  it('uses the login profile and token without probing administrative operations', async () => {
    const fetch = mockResponse(session)
    const { login, listCourts } = await import('./index')
    const loggedUser = await login(session.email, 'password123')
    expect(loggedUser.profile).toBe('CLIENT')
    expect(fetch).toHaveBeenCalledTimes(1)
    expect(fetch.mock.calls[0][0]).toBe('/users/login')
    expect(fetch.mock.calls[0][1].headers.Authorization).toBeUndefined()
    const { storeSession } = await import('../session')
    storeSession(loggedUser)
    await listCourts()
    expect(fetch.mock.calls[1][1].headers.Authorization).toBe('Bearer test-access-token')
    expect(fetch.mock.calls[1][1].headers['X-User-Id']).toBeUndefined()
  })

  it('discards sessions saved before token authentication', async () => {
    values.set('agendabeach.session', JSON.stringify({ id: 1, name: 'Old user', profile: 'ADMIN' }))
    const { getStoredSession } = await import('../session')
    expect(getStoredSession()).toBeNull()
    expect(values.has('agendabeach.session')).toBe(false)
  })

  it('restores the token and profile after a reload', async () => {
    values.set('agendabeach.session', JSON.stringify(session))
    const { getStoredSession } = await import('../session')
    expect(getStoredSession()).toEqual(session)
  })

  it('keeps authenticated requests working when localStorage is blocked', async () => {
    browser.localStorage.setItem = () => {
      throw new Error('Storage blocked')
    }
    const { storeSession } = await import('../session')
    storeSession(session)
    const fetch = mockResponse([])
    const { listCourts } = await import('./index')
    await listCourts()
    expect(fetch.mock.calls[0][1].headers.Authorization).toBe('Bearer test-access-token')
  })

  it('clears invalid tokens on 401 and notifies the application', async () => {
    const { storeSession, getStoredSession, SESSION_CLEARED_EVENT } = await import('../session')
    storeSession(session)
    const cleared = vi.fn()
    browser.addEventListener(SESSION_CLEARED_EVENT, cleared)
    mockResponse({}, 401)
    const { listCourts } = await import('./index')
    await expect(listCourts()).rejects.toMatchObject({ status: 401 })
    expect(getStoredSession()).toBeNull()
    expect(cleared).toHaveBeenCalledTimes(1)
  })

  it('preserves the session when a valid client lacks an administrative permission', async () => {
    const { storeSession, getStoredSession } = await import('../session')
    storeSession(session)
    mockResponse({}, 403)
    const { listUsers } = await import('./index')
    await expect(listUsers()).rejects.toMatchObject({ status: 403 })
    expect(getStoredSession()).toEqual(session)
  })

  it('does not clear a new login when a request from the previous session returns 401', async () => {
    const { storeSession, getStoredSession } = await import('../session')
    storeSession(session)
    let respond!: (response: Response) => void
    vi.stubGlobal(
      'fetch',
      vi.fn(
        () =>
          new Promise<Response>((resolve) => {
            respond = resolve
          }),
      ),
    )
    const { listCourts } = await import('./index')
    const pending = listCourts()
    const newSession = { ...session, accessToken: 'new-session-token' }
    storeSession(newSession)
    respond(new Response('{}', { status: 401 }))
    await expect(pending).rejects.toMatchObject({ status: 401 })
    expect(getStoredSession()).toEqual(newSession)
  })

  it('reports CPF errors returned as ProblemDetail', async () => {
    mockResponse({ detail: 'Invalid CPF' }, 400)
    const { register } = await import('./index')
    await expect(register('Client', session.email, 'password123', '11111111111')).rejects.toMatchObject({
      status: 400,
      message: 'CPF inválido. Confira os 11 dígitos.',
    })
  })

  it('revokes the current bearer token on logout', async () => {
    const { storeSession } = await import('../session')
    storeSession(session)
    const fetch = vi.fn().mockResolvedValue(new Response(null, { status: 200 }))
    vi.stubGlobal('fetch', fetch)
    const { logout } = await import('./index')
    await logout()
    expect(fetch.mock.calls[0][0]).toBe('/users/logout')
    expect(fetch.mock.calls[0][1].headers.Authorization).toBe('Bearer test-access-token')
  })
})
