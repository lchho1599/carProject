import { describe, expect, it } from 'vitest'
import { sorentoDetail } from '../../test/modelFixture'
import { calculateEstimatePrice, initialTrimId } from './price'

describe('간편견적 합계 계산', () => {
  it('트림가 + 색상 추가금 + 선택 옵션가', () => {
    expect(calculateEstimatePrice(sorentoDetail, { trimId: 18, colorId: 41, optionIds: [31, 32] })).toEqual({
      trimPrice: 48600000,
      colorPrice: 80000,
      optionPrice: 1650000,
      total: 50330000,
    })
  })

  it('색상·옵션을 고르지 않으면 트림가만', () => {
    expect(calculateEstimatePrice(sorentoDetail, { trimId: 17, colorId: undefined, optionIds: [] }).total).toBe(39900000)
  })

  it('이 모델에 없는 id 는 금액에 넣지 않는다', () => {
    expect(calculateEstimatePrice(sorentoDetail, { trimId: 999, colorId: 999, optionIds: [999] }).total).toBe(0)
  })

  it('주소의 trimId 가 이 모델 트림이면 선택하고, 아니면 첫 트림', () => {
    expect(initialTrimId(sorentoDetail, '18')).toBe(18)
    expect(initialTrimId(sorentoDetail, '999')).toBe(17)
    expect(initialTrimId(sorentoDetail, null)).toBe(17)
  })
})
