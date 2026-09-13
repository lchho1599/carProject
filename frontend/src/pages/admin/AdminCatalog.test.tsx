import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { setCsrfToken } from '../../lib/apiClient'
import { adminMe, renderAdmin } from '../../test/renderAdmin'
import { mockApi } from '../../test/renderWithProviders'

const adminBrands = [
  { id: 2, name: '기아', origin: 'DOMESTIC', logoUrl: null, sortOrder: 2, active: true },
  { id: 9, name: '단종모터스', origin: 'IMPORTED', logoUrl: null, sortOrder: 9, active: false },
]
const kiaModels = [
  { id: 9, brandId: 2, brandName: '기아', name: '쏘렌토', segment: '중형', bodyType: 'SUV', fuel: 'HYBRID', imageUrl: null, sortOrder: 1, active: true },
]
const sorentoAdmin = {
  model: kiaModels[0],
  trims: [{ id: 18, name: '프레스티지', price: 39900000, sortOrder: 1, active: true }],
  options: [],
  colors: [],
}
const validationError = (field: string, message: string) => ({
  status: 400,
  body: { code: 'VALIDATION_FAILED', message: '요청 값이 올바르지 않습니다.', fieldErrors: [{ field, message }] },
})

describe('관리자 상품 관리 화면', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    setCsrfToken(null)
  })

  it('차량 관리: 브랜드를 고르면 모델 목록을 보여주고, 브랜드 수정 창을 연다', async () => {
    mockApi({
      '/api/admin/auth/me': () => ({ body: adminMe }),
      '/api/admin/brands': () => ({ body: adminBrands }),
      '/api/admin/brands/2/models': () => ({ body: kiaModels }),
    })
    const { user } = renderAdmin('/admin/vehicles')

    const brandList = await screen.findByRole('region', { name: '브랜드' })
    expect(within(brandList).getByText('사용 안 함')).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: /쏘렌토/ })).toHaveAttribute('href', '/admin/vehicles/models/9')

    await user.click(screen.getByRole('button', { name: '기아 수정' }))
    const dialog = await screen.findByRole('dialog', { name: '브랜드 수정' })
    expect(within(dialog).getByLabelText(/브랜드명/)).toHaveValue('기아')
  })

  it('특가 등록: 브랜드→모델→세부모델을 고르고 한국 시간·숫자로 변환해 저장한다', async () => {
    let body: Record<string, unknown> | undefined
    let csrf: string | undefined
    mockApi({
      '/api/admin/auth/me': () => ({ body: adminMe }),
      '/api/admin/brands': () => ({ body: adminBrands }),
      '/api/admin/brands/2/models': () => ({ body: kiaModels }),
      '/api/admin/models/9': () => ({ body: sorentoAdmin }),
      '/api/admin/deals': (_url, init) => {
        if (init?.method === 'POST') {
          body = JSON.parse(String(init.body))
          csrf = (init.headers as Record<string, string>)['X-CSRF-TOKEN']
          return { status: 201, body: { id: 99 } }
        }
        return { body: [] }
      },
    })
    const { user, router } = renderAdmin('/admin/deals/new')

    await user.selectOptions(await screen.findByLabelText(/^브랜드/), await screen.findByRole('option', { name: '기아' }))
    await user.selectOptions(screen.getByLabelText(/^모델/), await screen.findByRole('option', { name: '쏘렌토' }))
    await user.selectOptions(screen.getByLabelText(/^세부모델/), await screen.findByRole('option', { name: /프레스티지/ }))
    await user.type(screen.getByLabelText(/표시 제목/), '추석 특가')
    await user.type(screen.getByLabelText(/^월 납입료/), '209,000')
    await user.type(screen.getByLabelText(/정가 월 납입료/), '230000')
    await user.clear(screen.getByLabelText(/노출 시작/))
    await user.type(screen.getByLabelText(/노출 시작/), '2026-09-20T09:00')
    await user.click(screen.getByRole('switch', { name: /공개/ }))
    await user.click(screen.getByRole('button', { name: '특가 등록' }))

    await vi.waitFor(() => expect(router.state.location.pathname).toBe('/admin/deals'))
    expect(csrf).toBe('csrf-abc')
    expect(body).toMatchObject({
      type: 'TIME_SALE',
      trimId: 18,
      title: '추석 특가',
      monthlyPrice: 209000,
      originalMonthly: 230000,
      leaseMonthly: null,
      periodMonths: 48,
      depositRate: 0,
      prepayRate: 30,
      startsAt: '2026-09-20T09:00:00+09:00',
      endsAt: null,
      published: true,
    })
  })

  it('특가 저장 시 서버 항목 오류를 해당 칸 아래에 보여준다', async () => {
    mockApi({
      '/api/admin/auth/me': () => ({ body: adminMe }),
      '/api/admin/brands': () => ({ body: adminBrands }),
      '/api/admin/deals': () => validationError('trimId', '차량(세부모델)을 선택해 주세요.'),
    })
    const { user } = renderAdmin('/admin/deals/new')

    await user.click(await screen.findByRole('button', { name: '특가 등록' }))

    expect(await screen.findByText('차량(세부모델)을 선택해 주세요.')).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('입력값을 확인해 주세요.')
  })

  it('배너 등록: 이미지를 올리면 미리보기가 나오고, 위험한 링크는 링크 칸에 오류로 보여준다', async () => {
    let uploadCount = 0
    mockApi({
      '/api/admin/auth/me': () => ({ body: adminMe }),
      '/api/admin/uploads': () => {
        uploadCount += 1
        return { status: 201, body: { url: `/api/files/2026/09/banner-${uploadCount}.png` } }
      },
      '/api/admin/banners': () => validationError('linkUrl', '주소는 / 로 시작하는 사이트 경로 또는 https:// 주소만 입력할 수 있습니다.'),
    })
    const { user } = renderAdmin('/admin/banners/new')

    const pcSection = await screen.findByRole('region', { name: '배너 이미지' })
    const png = new File([new Uint8Array([0x89, 0x50, 0x4e, 0x47])], 'banner.png', { type: 'image/png' })
    await user.upload(within(pcSection).getByLabelText(/PC 이미지/), png)

    expect(await screen.findByRole('img', { name: 'PC 이미지 미리보기' })).toHaveAttribute('src', '/api/files/2026/09/banner-1.png')

    await user.type(screen.getByLabelText(/배너 제목/), '테스트 배너')
    await user.type(screen.getByLabelText(/^링크/), 'javascript:alert(1)')
    await user.click(screen.getByRole('button', { name: '배너 등록' }))

    expect(await screen.findByText('주소는 / 로 시작하는 사이트 경로 또는 https:// 주소만 입력할 수 있습니다.')).toBeInTheDocument()
  })
})
