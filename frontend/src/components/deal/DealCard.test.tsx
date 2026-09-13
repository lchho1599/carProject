import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { makeDeal } from '../../test/fixtures'
import { renderWithProviders } from '../../test/renderWithProviders'
import { DealCard } from './DealCard'

describe('특가 카드', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('정가·할인율·월 납입료·조건을 보여준다', () => {
    renderWithProviders(<DealCard deal={makeDeal()} />)

    expect(screen.getByRole('heading', { name: '기아 쏘렌토' })).toBeInTheDocument()
    expect(screen.getByText('230,000원').tagName).toBe('DEL')
    expect(screen.getByText('9%')).toBeInTheDocument()
    expect(screen.getByText('209,000원')).toBeInTheDocument()
    expect(screen.getByText('리스 월 188,000원')).toBeInTheDocument()
    expect(screen.getByText(/48개월 · 보증금 0% · 선납금 30%/)).toBeInTheDocument()
    expect(screen.getByText('HOT SALE')).toBeInTheDocument()
  })

  it('타임특가의 견적 확인은 트림이 선택된 간편견적으로 연결된다', () => {
    renderWithProviders(<DealCard deal={makeDeal()} />)

    expect(screen.getByRole('link', { name: '견적 확인' })).toHaveAttribute('href', '/estimate/9?trimId=18')
  })

  it('무보증특가는 상담 모달을 열고 특가 정보를 보여준다', async () => {
    const { user } = renderWithProviders(
      <DealCard deal={makeDeal({ type: 'NO_DEPOSIT', originalMonthly: null, leaseMonthly: null, title: '기아 쏘렌토 무보증' })} />,
    )

    expect(screen.getByText(/보증금·선납금 0원/)).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: '견적 확인' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: '간편 상담신청' }))

    const dialog = await screen.findByRole('dialog', { name: '무료 상담 신청' })
    expect(within(dialog).getByText('기아 쏘렌토 무보증 · 209,000원/월')).toBeInTheDocument()
  })
})
