import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { setCsrfToken } from '../../lib/apiClient'
import { adminMe, renderAdmin } from '../../test/renderAdmin'
import { mockApi } from '../../test/renderWithProviders'

const accounts = [
  { id: 1, email: 'admin@test.local', name: '김관리', active: true, lastLoginAt: '2026-09-13T18:00:00+09:00', createdAt: '2026-09-01T10:00:00+09:00', me: true },
  { id: 2, email: 'staff@test.local', name: '이상담', active: true, lastLoginAt: null, createdAt: '2026-09-10T10:00:00+09:00', me: false },
]

type Call = { path: string; method?: string; csrf?: string; body: unknown }

function mockAccounts(overrides: Record<string, (url: URL, init?: RequestInit) => { status?: number; body: unknown }> = {}) {
  const calls: Call[] = []
  const record = (path: string, response: { status?: number; body: unknown }) => (_url: URL, init?: RequestInit) => {
    calls.push({ path, method: init?.method, csrf: (init?.headers as Record<string, string> | undefined)?.['X-CSRF-TOKEN'], body: init?.body ? JSON.parse(String(init.body)) : undefined })
    return response
  }
  mockApi({
    '/api/admin/auth/me': () => ({ body: adminMe }),
    '/api/admin/users': (url, init) => (init?.method === 'POST' ? record('/api/admin/users', { status: 201, body: { ...accounts[1], id: 3, email: 'new@test.local', name: '신규' } })(url, init) : { body: accounts }),
    '/api/admin/users/2': record('/api/admin/users/2', { body: { ...accounts[1], active: false } }),
    '/api/admin/users/me/password': record('/api/admin/users/me/password', { status: 204, body: null }),
    ...overrides,
  })
  return calls
}

describe('관리자 계정 화면', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    setCsrfToken(null)
  })

  it('본인 계정에는 비활성화·재설정 버튼이 없고, 다른 관리자를 비활성화하면 확인 후 CSRF 토큰과 함께 전송한다', async () => {
    const calls = mockAccounts()
    vi.stubGlobal('confirm', vi.fn(() => true))
    const { user } = renderAdmin('/admin/accounts')

    const table = await screen.findByRole('table')
    const myRow = within(table).getByText('admin@test.local').closest('tr')!
    expect(within(myRow).getByText('나')).toBeInTheDocument()
    expect(within(myRow).queryByRole('button', { name: /비활성화/ })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: '이상담 비활성화' }))

    expect(window.confirm).toHaveBeenCalled()
    await vi.waitFor(() => expect(calls).toContainEqual({ path: '/api/admin/users/2', method: 'PUT', csrf: 'csrf-abc', body: { name: '이상담', active: false } }))
    expect(await screen.findByRole('status')).toHaveTextContent('로그인 세션을 종료했습니다')
  })

  it('관리자 추가: 확인 값이 다르면 전송하지 않고, 맞으면 전송하며 서버 중복 오류를 보여준다', async () => {
    const calls = mockAccounts({
      '/api/admin/users': (_url, init) =>
        init?.method === 'POST'
          ? { status: 409, body: { code: 'DUPLICATED', message: '이미 등록된 이메일입니다.', fieldErrors: [] } }
          : { body: accounts },
    })
    const { user } = renderAdmin('/admin/accounts')

    await user.click(await screen.findByRole('button', { name: '관리자 추가' }))
    const dialog = await screen.findByRole('dialog', { name: '관리자 추가' })
    await user.type(within(dialog).getByLabelText(/^이메일/), 'staff@test.local')
    await user.type(within(dialog).getByLabelText(/^이름/), '중복')
    await user.type(within(dialog).getByLabelText(/^초기 비밀번호\*?$/), 'test1234')
    await user.type(within(dialog).getByLabelText(/초기 비밀번호 확인/), 'test9999')
    await user.click(within(dialog).getByRole('button', { name: '추가' }))

    expect(within(dialog).getByRole('alert')).toHaveTextContent('새 비밀번호와 확인 값이 다릅니다.')
    expect(calls).toHaveLength(0)

    await user.clear(within(dialog).getByLabelText(/초기 비밀번호 확인/))
    await user.type(within(dialog).getByLabelText(/초기 비밀번호 확인/), 'test1234')
    await user.click(within(dialog).getByRole('button', { name: '추가' }))

    expect(await within(dialog).findByText('이미 등록된 이메일입니다.')).toBeInTheDocument()
  })

  it('내 비밀번호 변경: 규칙에 맞지 않으면 막고, 맞으면 현재·새 비밀번호를 보내 완료를 알린다', async () => {
    const calls = mockAccounts()
    const { user } = renderAdmin('/admin/accounts')

    const card = await screen.findByRole('region', { name: '내 비밀번호 변경' })
    await user.type(within(card).getByLabelText('현재 비밀번호'), 'oldpass12')
    await user.type(within(card).getByLabelText('새 비밀번호'), 'onlyletters')
    await user.type(within(card).getByLabelText('새 비밀번호 확인'), 'onlyletters')
    await user.click(within(card).getByRole('button', { name: '비밀번호 변경' }))

    expect(within(card).getByRole('alert')).toHaveTextContent('영문과 숫자')
    expect(calls).toHaveLength(0)

    await user.clear(within(card).getByLabelText('새 비밀번호'))
    await user.type(within(card).getByLabelText('새 비밀번호'), 'newpass34')
    await user.clear(within(card).getByLabelText('새 비밀번호 확인'))
    await user.type(within(card).getByLabelText('새 비밀번호 확인'), 'newpass34')
    await user.click(within(card).getByRole('button', { name: '비밀번호 변경' }))

    expect(await within(card).findByRole('status')).toHaveTextContent('비밀번호를 변경했습니다.')
    expect(calls).toEqual([
      { path: '/api/admin/users/me/password', method: 'PUT', csrf: 'csrf-abc', body: { currentPassword: 'oldpass12', newPassword: 'newpass34' } },
    ])
  })
})
