import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { setCsrfToken } from '../../lib/apiClient'
import { adminMe, renderAdmin } from '../../test/renderAdmin'
import { mockApi } from '../../test/renderWithProviders'

const listItem = {
  id: 42,
  type: 'ESTIMATE',
  status: 'NEW',
  maskedName: '홍*동',
  maskedPhone: '010-****-5678',
  vehicleName: '기아 쏘렌토',
  totalPrice: 39900000,
  createdAt: '2026-09-13T18:20:00+09:00',
}

const detail = {
  id: 42,
  type: 'ESTIMATE',
  status: 'NEW',
  name: '홍길동',
  phone: '010-1234-5678',
  vehicleName: '기아 쏘렌토',
  vehicleSnapshot: { brand: '기아', model: '쏘렌토', trim: '프레스티지', trimPrice: 39900000, options: [{ name: '선루프', price: 1200000 }] },
  conditions: { useType: 'RENT', periodMonths: 48 },
  conditionSummary: '장기렌트 · 48개월',
  totalPrice: 41100000,
  dealId: null,
  instantStockId: null,
  agreePrivacy: true,
  agreeMarketing: false,
  agreedAt: '2026-09-13T18:20:00+09:00',
  sourceUrl: '/estimate/9',
  utm: { utm_source: 'naver' },
  userAgent: 'test-agent',
  createdAt: '2026-09-13T18:20:00+09:00',
  updatedAt: '2026-09-13T18:20:00+09:00',
  notes: [],
  notifications: [{ recipient: 'ops@test.local', status: 'SENT', createdAt: '2026-09-13T18:20:05+09:00' }],
}

describe('관리자 상담 신청 화면', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    setCsrfToken(null)
  })

  it('목록은 마스킹된 정보를 보여주고, 필터를 적용하면 주소와 조회 조건에 반영한다', async () => {
    const api = mockApi({
      '/api/admin/auth/me': () => ({ body: adminMe }),
      '/api/admin/leads': () => ({ body: { content: [listItem], page: 0, size: 20, totalElements: 1, totalPages: 1 } }),
    })
    const { user, router } = renderAdmin('/admin/leads')

    const table = await screen.findByRole('table')
    expect(within(table).getByText('홍*동')).toBeInTheDocument()
    expect(within(table).getByText('010-****-5678')).toBeInTheDocument()
    expect(within(table).getByText('2026-09-13 18:20')).toBeInTheDocument()

    await user.selectOptions(screen.getByLabelText('상태'), '신규')
    await user.type(screen.getByLabelText('검색'), '5678')
    await user.click(screen.getByRole('button', { name: '검색' }))

    expect(router.state.location.search).toBe('?status=NEW&q=5678')
    expect(api.requestedUrls()).toContain('/api/admin/leads?status=NEW&q=5678')
    expect(screen.getByRole('link', { name: 'CSV 다운로드' })).toHaveAttribute(
      'href',
      '/api/admin/leads/export.csv?status=NEW&q=5678',
    )
  })

  it('상세에서 상태를 저장하고 메모를 남기면 CSRF 토큰과 함께 전송한다', async () => {
    const calls: { path: string; method?: string; csrf?: string; body: unknown }[] = []
    const record = (path: string) => (_url: URL, init?: RequestInit) => {
      calls.push({
        path,
        method: init?.method,
        csrf: (init?.headers as Record<string, string> | undefined)?.['X-CSRF-TOKEN'],
        body: JSON.parse(String(init?.body)),
      })
      return path.endsWith('notes')
        ? { status: 201, body: { id: 1, content: '부재', adminName: '김관리', createdAt: detail.createdAt } }
        : { status: 204, body: null }
    }
    mockApi({
      '/api/admin/auth/me': () => ({ body: adminMe }),
      '/api/admin/leads/42': (url, init) => (init?.method === 'PATCH' ? record('/api/admin/leads/42')(url, init) : { body: detail }),
      '/api/admin/leads/42/notes': record('/api/admin/leads/42/notes'),
    })
    const { user } = renderAdmin('/admin/leads/42')

    expect(await screen.findByRole('heading', { name: '홍길동' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: '010-1234-5678' })).toHaveAttribute('href', 'tel:010-1234-5678')
    expect(screen.getByText('프레스티지')).toBeInTheDocument()
    expect(screen.getByText('선루프')).toBeInTheDocument()
    expect(screen.getByText('utm_source=naver')).toBeInTheDocument()

    await user.selectOptions(screen.getByLabelText('상태 변경'), '상담중')
    await user.click(screen.getByRole('button', { name: '저장' }))
    await user.type(screen.getByLabelText('메모 내용'), '1차 통화 부재')
    await user.click(screen.getByRole('button', { name: '메모 추가' }))

    await vi.waitFor(() => expect(calls).toHaveLength(2))
    expect(calls[0]).toEqual({ path: '/api/admin/leads/42', method: 'PATCH', csrf: 'csrf-abc', body: { status: 'IN_PROGRESS' } })
    expect(calls[1]).toEqual({ path: '/api/admin/leads/42/notes', method: 'POST', csrf: 'csrf-abc', body: { content: '1차 통화 부재' } })
  })
})
