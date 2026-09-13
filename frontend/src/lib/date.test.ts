import { describe, expect, it } from 'vitest'
import { dDayLabel } from './date'

// 기준 시각: 한국 시간 2026-09-13 18:00
const now = new Date('2026-09-13T18:00:00+09:00')

describe('특가 마감 표시', () => {
  it('종료일이 없거나 이미 지났으면 표시하지 않는다', () => {
    expect(dDayLabel(null, now)).toBeNull()
    expect(dDayLabel('2026-09-13T17:59:00+09:00', now)).toBeNull()
  })

  it('같은 날 마감이면 "오늘 마감"', () => {
    expect(dDayLabel('2026-09-13T23:59:00+09:00', now)).toBe('오늘 마감')
  })

  it('한국 날짜 기준으로 남은 일수를 센다', () => {
    // 한국 시간으로는 다음 날 새벽이므로 D-1 (UTC로 계산하면 같은 날로 잘못 셀 수 있음)
    expect(dDayLabel('2026-09-14T01:00:00+09:00', now)).toBe('D-1')
    expect(dDayLabel('2026-09-16T10:00:00+09:00', now)).toBe('D-3')
  })
})
