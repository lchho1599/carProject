import { screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeDeal } from '../../test/fixtures'
import { mockApi, renderWithProviders } from '../../test/renderWithProviders'
import { DealsPage } from './DealsPage'

describe('특가 목록 화면', () => {
  afterEach(() => vi.unstubAllGlobals())

  function mockDeals() {
    return mockApi({
      '/api/deals': (url) =>
        url.searchParams.get('type') === 'NO_DEPOSIT'
          ? { body: [makeDeal({ id: 20, type: 'NO_DEPOSIT', title: '무보증 상품', vehicle: { ...makeDeal().vehicle, modelName: '스포티지' } })] }
          : { body: [makeDeal()] },
    })
  }

  it('주소의 type 파라미터로 탭을 정하고, 탭을 바꾸면 해당 유형을 조회한다', async () => {
    const api = mockDeals()
    const { user, router } = renderWithProviders(<DealsPage />, { path: '/deals?type=time', routePath: '/deals' })

    expect(await screen.findByRole('heading', { name: '기아 쏘렌토' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '타임특가' })).toHaveAttribute('aria-pressed', 'true')

    await user.click(screen.getByRole('button', { name: '무보증특가' }))

    expect(await screen.findByRole('heading', { name: '기아 스포티지' })).toBeInTheDocument()
    expect(router.state.location.search).toBe('?type=nodeposit')
    expect(api.requestedUrls()).toEqual(expect.arrayContaining(['/api/deals?type=TIME_SALE', '/api/deals?type=NO_DEPOSIT']))
  })

  it('특가가 없으면 안내 문구를 보여준다', async () => {
    mockApi({ '/api/deals': () => ({ body: [] }) })
    renderWithProviders(<DealsPage />, { path: '/deals', routePath: '/deals' })

    expect(await screen.findByText(/현재 진행 중인 특가가 없습니다/)).toBeInTheDocument()
  })

  it('불러오기 실패 시 다시 시도 버튼을 보여준다', async () => {
    mockApi({ '/api/deals': () => ({ status: 500, body: { code: 'INTERNAL_ERROR', message: '오류' } }) })
    renderWithProviders(<DealsPage />, { path: '/deals', routePath: '/deals' })

    expect(await screen.findByRole('button', { name: '다시 시도' })).toBeInTheDocument()
  })
})
