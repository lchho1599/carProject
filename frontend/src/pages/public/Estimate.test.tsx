import { screen, waitFor, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { brands } from '../../test/fixtures'
import { sorentoDetail } from '../../test/modelFixture'
import { mockApi, renderWithProviders } from '../../test/renderWithProviders'
import { EstimateDetailPage } from './EstimateDetailPage'
import { EstimatePage } from './EstimatePage'

describe('간편견적 1단계 (차량 선택)', () => {
  afterEach(() => vi.unstubAllGlobals())

  function mockVehicles() {
    return mockApi({
      '/api/brands': (url) => ({ body: brands.filter((brand) => brand.origin === (url.searchParams.get('origin') ?? 'DOMESTIC')) }),
      '/api/brands/2/models': () => ({
        body: [{ id: 9, name: '쏘렌토', segment: '중형', bodyType: 'SUV', fuel: 'HYBRID', imageUrl: null, minPrice: 39900000 }],
      }),
    })
  }

  it('국산·수입 탭으로 브랜드를 거르고, 브랜드를 고르면 모델 카드가 2단계로 연결된다', async () => {
    const api = mockVehicles()
    const { user, router } = renderWithProviders(<EstimatePage />, { path: '/estimate', routePath: '/estimate' })

    expect(await screen.findByText('위에서 브랜드를 선택하면 모델이 나타납니다.')).toBeInTheDocument()
    await user.click(await screen.findByRole('button', { name: /기아/ }))

    const modelLink = await screen.findByRole('link', { name: /쏘렌토/ })
    expect(modelLink).toHaveAttribute('href', '/estimate/9')
    expect(within(modelLink).getByText('3,990만원~')).toBeInTheDocument()
    expect(router.state.location.search).toBe('?brand=2')

    await user.click(screen.getByRole('button', { name: '수입차' }))
    expect(await screen.findByRole('button', { name: /BMW/ })).toBeInTheDocument()
    expect(api.requestedUrls()).toContain('/api/brands?origin=IMPORTED')
  })
})

describe('간편견적 2단계 (옵션·조건 선택)', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('특가에서 넘어온 트림을 선택하고, 고른 사양에 따라 합계가 바뀌며, 모든 선택을 담아 신청한다', async () => {
    let leadBody: Record<string, unknown> | undefined
    mockApi({
      '/api/models/9': () => ({ body: sorentoDetail }),
      '/api/leads': (_url, init) => {
        leadBody = JSON.parse(String(init?.body))
        return { status: 201, body: { id: 77 } }
      },
    })
    const { user } = renderWithProviders(<EstimateDetailPage />, { path: '/estimate/9?trimId=18', routePath: '/estimate/:modelId' })

    expect(await screen.findByRole('heading', { name: '쏘렌토' })).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: /하이브리드 시그니처/ })).toBeChecked()
    const summary = screen.getByRole('region', { name: '견적 요약' })
    expect(within(summary).getByText('48,600,000원', { selector: 'strong' })).toBeInTheDocument()

    await user.click(screen.getByRole('radio', { name: /스노우 화이트 펄/ }))
    await user.click(screen.getByRole('checkbox', { name: /파노라마 선루프/ }))
    expect(within(summary).getByText('49,880,000원', { selector: 'strong' })).toBeInTheDocument()

    await user.click(screen.getByRole('radio', { name: '리스' }))
    await user.click(screen.getByRole('radio', { name: '60개월' }))
    await user.click(screen.getByRole('radio', { name: '무제한' }))
    await user.click(screen.getByRole('radio', { name: '700점 이상' }))

    const apply = screen.getByRole('region', { name: '견적 신청' })
    await user.type(within(apply).getByLabelText('이름'), '김견적')
    await user.type(within(apply).getByLabelText('연락처'), '01012345678')
    await user.click(within(apply).getByRole('checkbox', { name: /개인정보 수집·이용 동의/ }))
    await user.click(within(apply).getByRole('button', { name: '견적 상담 신청' }))

    expect(await screen.findByText('신청 완료 화면')).toBeInTheDocument()
    await waitFor(() =>
      expect(leadBody).toMatchObject({
        type: 'ESTIMATE',
        trimId: 18,
        colorId: 41,
        optionIds: [31],
        conditions: {
          useType: 'LEASE',
          periodMonths: 60,
          depositRate: 0,
          prepayRate: 30,
          insuranceAge: 26,
          annualMileage: 0,
          creditScore: 'OVER_700',
        },
      }),
    )
    expect(leadBody).not.toHaveProperty('totalPrice')
  })

  it('없는 모델이면 차량 다시 선택 안내를 보여준다', async () => {
    mockApi({ '/api/models/999': () => ({ status: 404, body: { code: 'NOT_FOUND', message: '없음', fieldErrors: [] } }) })
    renderWithProviders(<EstimateDetailPage />, { path: '/estimate/999', routePath: '/estimate/:modelId' })

    expect(await screen.findByRole('heading', { name: '견적 가능한 차량을 찾을 수 없습니다' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: '차량 다시 선택하기' })).toHaveAttribute('href', '/estimate')
  })
})
