import { screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { brands } from '../../test/fixtures'
import { mockApi, renderWithProviders } from '../../test/renderWithProviders'
import { QuickEstimateForm } from './QuickEstimateForm'

describe('메인 빠른견적', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('브랜드를 고르면 모델을 불러오고, 선택한 차량·기간을 함께 신청한다', async () => {
    let leadBody: Record<string, unknown> | undefined
    mockApi({
      '/api/brands': () => ({ body: brands }),
      '/api/brands/2/models': () => ({
        body: [{ id: 9, name: '쏘렌토', segment: '중형', bodyType: 'SUV', fuel: 'HYBRID', imageUrl: null, minPrice: 39900000 }],
      }),
      '/api/leads': (_url, init) => {
        leadBody = JSON.parse(String(init?.body))
        return { status: 201, body: { id: 100 } }
      },
    })
    const { user } = renderWithProviders(<QuickEstimateForm />)

    const modelSelect = screen.getByLabelText('모델')
    expect(modelSelect).toBeDisabled()

    await user.selectOptions(screen.getByLabelText('브랜드'), await screen.findByRole('option', { name: '기아' }))
    await user.selectOptions(modelSelect, await screen.findByRole('option', { name: '쏘렌토' }))
    await user.click(screen.getByLabelText('60개월'))
    await user.type(screen.getByLabelText('이름'), '김빠른')
    await user.type(screen.getByLabelText('연락처'), '01012345678')
    await user.click(screen.getByRole('checkbox', { name: /개인정보 수집·이용 동의/ }))
    await user.click(screen.getByRole('button', { name: '무료 견적 받기' }))

    expect(await screen.findByText('신청 완료 화면')).toBeInTheDocument()
    await waitFor(() =>
      expect(leadBody).toMatchObject({ type: 'QUICK', brandId: 2, modelId: 9, conditions: { periodMonths: 60 } }),
    )
  })
})
