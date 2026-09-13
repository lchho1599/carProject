import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { brands, makeStock } from '../../test/fixtures'
import { mockApi, renderWithProviders } from '../../test/renderWithProviders'
import { InstantPage } from './InstantPage'

const k5 = makeStock()
const ev3 = makeStock({ id: 6, exteriorColor: '딥 블루', vehicle: { ...makeStock().vehicle, modelName: 'EV3', fuel: 'EV' } })
const reserved = makeStock({ id: 9, status: 'RESERVED', vehicle: { ...makeStock().vehicle, brandName: '현대', modelName: '싼타페' } })

describe('즉시출고 화면', () => {
  afterEach(() => vi.unstubAllGlobals())

  function mockInstant() {
    return mockApi({
      '/api/brands': () => ({ body: brands.filter((brand) => brand.origin === 'DOMESTIC') }),
      '/api/instant': (url) => {
        const items = url.searchParams.get('fuel') === 'EV' ? [ev3] : [k5, ev3, reserved]
        return { body: { totalCount: items.length, items } }
      },
    })
  }

  it('총 대수와 차량 카드를 보여주고 예약중 차량을 표시한다', async () => {
    mockInstant()
    renderWithProviders(<InstantPage />, { path: '/instant', routePath: '/instant' })

    expect(await screen.findByText('3대')).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(3)
    const reservedCard = screen.getByRole('heading', { name: '현대 싼타페' }).closest('article')!
    expect(within(reservedCard).getByText('예약중')).toBeInTheDocument()
    expect(within(reservedCard).getByRole('button', { name: '대기 상담신청' })).toBeInTheDocument()
  })

  it('전기차 필터는 연료 조건으로 조회하고 주소에 남긴다', async () => {
    const api = mockInstant()
    const { user, router } = renderWithProviders(<InstantPage />, { path: '/instant', routePath: '/instant' })
    await screen.findByText('3대')

    await user.click(screen.getByRole('button', { name: '전기차' }))

    expect(await screen.findByText('1대')).toBeInTheDocument()
    expect(api.requestedUrls()).toContain('/api/instant?fuel=EV')
    expect(router.state.location.search).toBe('?kind=ev')
  })

  it('국산 브랜드 탭과 수입 탭은 각각 brandId, origin 으로 조회한다', async () => {
    const api = mockInstant()
    const { user } = renderWithProviders(<InstantPage />, { path: '/instant', routePath: '/instant' })

    await user.click(await screen.findByRole('button', { name: '기아' }))
    await user.click(screen.getByRole('button', { name: '수입' }))

    expect(api.requestedUrls()).toEqual(expect.arrayContaining(['/api/instant?brandId=2', '/api/instant?origin=IMPORTED']))
  })

  it('카드의 상담신청은 차량 정보를 담아 모달을 연다', async () => {
    mockInstant()
    const { user } = renderWithProviders(<InstantPage />, { path: '/instant', routePath: '/instant' })

    const card = (await screen.findByRole('heading', { name: '기아 EV3' })).closest('article')!
    await user.click(within(card).getByRole('button', { name: '간편 상담신청' }))

    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByText('즉시출고 · 기아 EV3 (딥 블루)')).toBeInTheDocument()
  })
})
