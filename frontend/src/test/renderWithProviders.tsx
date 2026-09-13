import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { ReactElement } from 'react'
import { createMemoryRouter, Outlet, RouterProvider } from 'react-router'
import { vi } from 'vitest'
import { ConsultModalProvider } from '../components/lead/ConsultModal'

type Options = {
  /** 처음 열 주소 (기본 '/') */
  path?: string
  /** 화면 요소를 둘 라우트 경로 (기본 '*') */
  routePath?: string
}

/**
 * 라우터 + React Query + 상담 모달을 갖춘 상태로 화면을 렌더링한다.
 * 신청 완료 이동을 확인할 수 있도록 /complete 라우트를 함께 둔다.
 */
export function renderWithProviders(element: ReactElement, { path = '/', routePath = '*' }: Options = {}) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(
    [
      {
        element: (
          <ConsultModalProvider>
            <Outlet />
          </ConsultModalProvider>
        ),
        children: [
          { path: '/complete', element: <p>신청 완료 화면</p> },
          { path: routePath, element },
        ],
      },
    ],
    { initialEntries: [path] },
  )
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
  return { user: userEvent.setup(), router }
}

type RouteHandler = (url: URL, init?: RequestInit) => { status?: number; body: unknown }

/**
 * 주소별로 가짜 API 응답을 돌려주는 fetch — 처리하지 않는 주소는 404.
 * 반환값의 calls 로 요청 주소를 확인할 수 있다.
 */
export function mockApi(routes: Record<string, RouteHandler>) {
  const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = new URL(String(input), 'http://localhost')
    const handler = routes[url.pathname]
    const result = handler ? handler(url, init) : { status: 404, body: { code: 'NOT_FOUND', message: 'not found' } }
    const status = result.status ?? 200
    // 204(본문 없음) 응답에 본문을 넣으면 Response 생성이 실패하므로 비운다
    return new Response(status === 204 ? null : JSON.stringify(result.body), {
      status,
      headers: { 'Content-Type': 'application/json' },
    })
  })
  vi.stubGlobal('fetch', fetchMock)
  return {
    fetchMock,
    requestedUrls: () => fetchMock.mock.calls.map(([input]) => String(input)),
  }
}
