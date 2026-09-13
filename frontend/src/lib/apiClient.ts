// 백엔드 API 호출 공통 함수
// 로컬: VITE_API_BASE_URL을 비워 두면 Vite 프록시(/api → localhost:8080)를 사용한다.
// 운영: VITE_API_BASE_URL=https://api.example.com
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export type FieldError = { field: string; message: string }

/** 백엔드 오류 응답 { code, message, fieldErrors } 를 담는 예외 */
export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: FieldError[]

  constructor(status: number, code: string, message: string, fieldErrors: FieldError[] = []) {
    super(message)
    this.status = status
    this.code = code
    this.fieldErrors = fieldErrors
  }
}

const NETWORK_ERROR_MESSAGE = '네트워크 연결을 확인한 뒤 다시 시도해 주세요.'
const CSRF_HEADER = 'X-CSRF-TOKEN'

// 관리자 CSRF 토큰 — 로그인·/me 응답으로 받아 메모리에만 보관한다 (쿠키·저장소에 남기지 않음)
let csrfToken: string | null = null

export function setCsrfToken(token: string | null): void {
  csrfToken = token
}

/** 다운로드 링크 등 fetch 가 아닌 곳에서 쓸 전체 API 주소 */
export function apiUrl(path: string): string {
  return `${API_BASE_URL}${path}`
}

async function request<T>(path: string, init: RequestInit): Promise<T> {
  const method = (init.method ?? 'GET').toUpperCase()
  const headers: Record<string, string> = { Accept: 'application/json', ...(init.headers as Record<string, string>) }
  if (method !== 'GET' && csrfToken) {
    headers[CSRF_HEADER] = csrfToken
  }

  let res: Response
  try {
    res = await fetch(apiUrl(path), { credentials: 'include', ...init, method, headers })
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR', NETWORK_ERROR_MESSAGE)
  }

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new ApiError(
      res.status,
      body?.code ?? 'UNKNOWN_ERROR',
      body?.message ?? '일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
      Array.isArray(body?.fieldErrors) ? body.fieldErrors : [],
    )
  }
  if (res.status === 204) {
    return undefined as T
  }
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export function apiGet<T>(path: string, init?: RequestInit): Promise<T> {
  return request<T>(path, { ...init, method: 'GET' })
}

export function apiPost<T>(path: string, body?: unknown, init?: RequestInit): Promise<T> {
  return request<T>(path, {
    ...init,
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

export function apiPut<T>(path: string, body: unknown, init?: RequestInit): Promise<T> {
  return request<T>(path, {
    ...init,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export function apiDelete<T = void>(path: string, init?: RequestInit): Promise<T> {
  return request<T>(path, { ...init, method: 'DELETE' })
}

/** 파일 업로드 — Content-Type 은 브라우저가 multipart 경계값과 함께 정하도록 지정하지 않는다 */
export function apiUpload<T>(path: string, file: File, fieldName = 'file'): Promise<T> {
  const form = new FormData()
  form.append(fieldName, file)
  return request<T>(path, { method: 'POST', body: form })
}

export function apiPatch<T>(path: string, body: unknown, init?: RequestInit): Promise<T> {
  return request<T>(path, {
    ...init,
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}
