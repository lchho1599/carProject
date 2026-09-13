import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiGet, apiPatch, apiPost, apiUpload, setCsrfToken } from './apiClient'

const fetchMock = vi.fn()

describe('API 공통 호출', () => {
  afterEach(() => {
    fetchMock.mockReset()
    vi.unstubAllGlobals()
    setCsrfToken(null)
  })

  /** 호출마다 새 Response 를 만든다 (응답 본문은 한 번만 읽을 수 있음) */
  function stub(createResponse: () => Response) {
    fetchMock.mockImplementation(async () => createResponse())
    vi.stubGlobal('fetch', fetchMock)
  }

  it('변경 요청에는 CSRF 토큰 헤더를 붙이고 조회 요청에는 붙이지 않는다', async () => {
    setCsrfToken('token-123')
    stub(() => new Response('{}', { status: 200 }))

    await apiGet('/api/admin/leads')
    await apiPost('/api/admin/leads/1/notes', { content: '메모' })

    const [, getInit] = fetchMock.mock.calls[0]
    const [, postInit] = fetchMock.mock.calls[1]
    expect(getInit.headers['X-CSRF-TOKEN']).toBeUndefined()
    expect(postInit.headers['X-CSRF-TOKEN']).toBe('token-123')
    expect(postInit.credentials).toBe('include')
  })

  it('파일 업로드는 FormData 로 보내고 Content-Type 은 브라우저에 맡기며 CSRF 토큰을 붙인다', async () => {
    setCsrfToken('token-upload')
    stub(() => new Response('{"url":"/api/files/2026/09/a.png"}', { status: 201 }))
    const file = new File([new Uint8Array([0x89, 0x50])], 'car.png', { type: 'image/png' })

    await expect(apiUpload('/api/admin/uploads', file)).resolves.toEqual({ url: '/api/files/2026/09/a.png' })

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/admin/uploads')
    expect(init.body).toBeInstanceOf(FormData)
    expect((init.body as FormData).get('file')).toBe(file)
    expect(init.headers['Content-Type']).toBeUndefined()
    expect(init.headers['X-CSRF-TOKEN']).toBe('token-upload')
  })

  it('본문 없는 204 응답을 처리한다', async () => {
    stub(() => new Response(null, { status: 204 }))

    await expect(apiPatch('/api/admin/leads/1', { status: 'NEW' })).resolves.toBeUndefined()
  })

  it('오류 응답을 코드·메시지·필드 오류가 담긴 ApiError 로 바꾼다', async () => {
    stub(
      () =>
        new Response(JSON.stringify({ code: 'CSRF_TOKEN_INVALID', message: '보안 토큰 만료', fieldErrors: [] }), {
          status: 403,
        }),
    )

    const error = await apiPost('/api/admin/leads/1/notes', {}).catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 403, code: 'CSRF_TOKEN_INVALID', message: '보안 토큰 만료' })
  })
})
