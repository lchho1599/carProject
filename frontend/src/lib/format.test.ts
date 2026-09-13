import { describe, expect, it } from 'vitest'
import { formatManwon, formatMonthly, formatPhoneInput, formatWon } from './format'

describe('금액 표기', () => {
  it('원 단위에 쉼표를 넣는다', () => {
    expect(formatWon(39900000)).toBe('39,900,000원')
    expect(formatMonthly(209000)).toBe('월 209,000원')
  })

  it('만원 단위로 반올림한다', () => {
    expect(formatManwon(39900000)).toBe('3,990만원')
    expect(formatManwon(60635000)).toBe('6,064만원')
  })
})

describe('휴대폰 번호 입력 하이픈', () => {
  it.each([
    ['010', '010'],
    ['0101234', '010-1234'],
    ['01012345678', '010-1234-5678'],
    ['010-1234-5678', '010-1234-5678'],
    ['0111234567', '011-123-4567'],
    ['010123456789999', '010-1234-5678'],
    ['abc010def1234', '010-1234'],
  ])('%s → %s', (input, expected) => {
    expect(formatPhoneInput(input)).toBe(expected)
  })
})
