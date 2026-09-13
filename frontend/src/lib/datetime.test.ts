import { describe, expect, it } from 'vitest'
import { fromDatetimeLocal, nowDatetimeLocal, toDatetimeLocal } from './datetime'

describe('관리자 날짜 입력 변환 (한국 시간)', () => {
  it('서버 시각을 한국 시간 입력칸 값으로 바꾼다', () => {
    expect(toDatetimeLocal('2026-09-13T18:20:00+09:00')).toBe('2026-09-13T18:20')
    // UTC 로 온 값도 한국 시간으로 표시
    expect(toDatetimeLocal('2026-09-13T15:00:00Z')).toBe('2026-09-14T00:00')
    expect(toDatetimeLocal(null)).toBe('')
  })

  it('입력칸 값을 +09:00 시각으로 바꾸고 빈 값은 null', () => {
    expect(fromDatetimeLocal('2026-09-13T18:20')).toBe('2026-09-13T18:20:00+09:00')
    expect(fromDatetimeLocal('')).toBeNull()
  })

  it('지금 시각을 한국 시간으로', () => {
    expect(nowDatetimeLocal(new Date('2026-09-13T23:30:00Z'))).toBe('2026-09-14T08:30')
  })
})
