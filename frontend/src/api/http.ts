import { clearStoredSession, getStoredSession } from '../session'

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const fallbackMessages: Record<number, string> = {
  400: 'Dados inválidos. Revise as informações e tente de novo.',
  401: 'Sua sessão expirou. Entre novamente para continuar.',
  403: 'Você não tem permissão para executar esta ação.',
  404: 'Registro não encontrado.',
  409: 'A operação conflita com dados já existentes. Revise as informações.',
  500: 'Não foi possível concluir a operação. Verifique os dados e tente de novo.',
}

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  auth?: boolean
}

async function readErrorMessage(response: Response) {
  try {
    const data = await response.json()
    if (data?.detail === 'Invalid CPF') return 'CPF inválido. Confira os 11 dígitos.'
    if (typeof data?.detail === 'string' && data.detail.trim()) return data.detail
    if (data && typeof data.message === 'string' && data.message.trim()) return data.message
  } catch {
    /* resposta sem corpo JSON */
  }

  return fallbackMessages[response.status] ?? `Falha na requisição (HTTP ${response.status}).`
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, auth = true } = options
  const token = auth ? getStoredSession()?.accessToken : undefined
  const headers: Record<string, string> = { Accept: 'application/json' }

  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let response: Response
  try {
    response = await fetch(path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, 'Não foi possível falar com o servidor. Verifique se a API está no ar.')
  }

  if (!response.ok) {
    const message = await readErrorMessage(response)
    if (response.status === 401 && token && getStoredSession()?.accessToken === token) clearStoredSession()
    throw new ApiError(response.status, message)
  }

  if (response.status === 204) return undefined as T

  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}
