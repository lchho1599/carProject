import { screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { setCsrfToken } from '../../lib/apiClient'
import { adminMe, renderAdmin } from '../../test/renderAdmin'
import { mockApi } from '../../test/renderWithProviders'

const unauthorized = { status: 401, body: { code: 'UNAUTHORIZED', message: '로그인이 필요합니다.', fieldErrors: [] } }

describe('관리자 로그인·인증 가드', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    setCsrfToken(null)
  })

  it('로그인하지 않았으면 원래 주소를 기억한 채 로그인 화면으로 보낸다', async () => {
    mockApi({ '/api/admin/auth/me': () => unauthorized })
    const { router } = renderAdmin('/admin/leads?status=NEW')

    expect(await screen.findByRole('heading', { name: /관리자/ })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/admin/login')
    expect(new URLSearchParams(router.state.location.search).get('redirect')).toBe('/admin/leads?status=NEW')
  })

  it('로그인에 성공하면 기억한 주소로 돌아가고 관리자 이름을 보여준다', async () => {
    let loggedIn = false
    let loginBody: unknown
    mockApi({
      '/api/admin/auth/me': () => (loggedIn ? { body: adminMe } : unauthorized),
      '/api/admin/auth/login': (_url, init) => {
        loginBody = JSON.parse(String(init?.body))
        loggedIn = true
        return { body: adminMe }
      },
      '/api/admin/dashboard': () => ({
        body: {
          todayCount: 3,
          weekCount: 5,
          monthCount: 9,
          totalCount: 20,
          statusCounts: { NEW: 3, IN_PROGRESS: 2, CONTRACTED: 1, NO_ANSWER: 0, CANCELED: 0 },
          recentLeads: [],
        },
      }),
    })
    const { user, router } = renderAdmin('/admin/login?redirect=%2Fadmin')

    await user.type(await screen.findByLabelText('이메일'), 'admin@test.local')
    await user.type(screen.getByLabelText('비밀번호'), 'test-only-value')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    expect(await screen.findByRole('heading', { name: '대시보드' })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/admin')
    expect(screen.getByText('김관리')).toBeInTheDocument()
    expect(loginBody).toEqual({ email: 'admin@test.local', password: 'test-only-value' })
  })

  it('로그인에 실패하면 서버 안내 문구를 보여준다', async () => {
    mockApi({
      '/api/admin/auth/me': () => unauthorized,
      '/api/admin/auth/login': () => ({
        status: 401,
        body: { code: 'LOGIN_FAILED', message: '이메일 또는 비밀번호가 올바르지 않습니다.', fieldErrors: [] },
      }),
    })
    const { user } = renderAdmin('/admin/login')

    await user.type(await screen.findByLabelText('이메일'), 'admin@test.local')
    await user.type(screen.getByLabelText('비밀번호'), 'wrong')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('이메일 또는 비밀번호가 올바르지 않습니다.')
  })

  it('redirect 에 외부 주소를 넣어도 관리자 화면으로만 이동한다', async () => {
    mockApi({
      '/api/admin/auth/me': () => ({ body: adminMe }),
      '/api/admin/dashboard': () => ({
        body: { todayCount: 0, weekCount: 0, monthCount: 0, totalCount: 0, statusCounts: {}, recentLeads: [] },
      }),
    })
    const { router } = renderAdmin('/admin/login?redirect=https%3A%2F%2Fevil.example')

    await waitFor(() => expect(router.state.location.pathname).toBe('/admin'))
  })
})
