const DAY_MS = 24 * 60 * 60 * 1000

/**
 * 특가 마감 표시: 오늘 마감 → "오늘 마감", 3일 남음 → "D-3", 종료일이 없거나 지났으면 null.
 * 날짜 차이는 한국 시간 달력 기준으로 계산한다.
 */
export function dDayLabel(endsAt: string | null, now: Date = new Date()): string | null {
  if (!endsAt) return null
  const end = new Date(endsAt)
  if (Number.isNaN(end.getTime()) || end.getTime() <= now.getTime()) return null

  const days = Math.round((kstDayStart(end) - kstDayStart(now)) / DAY_MS)
  return days <= 0 ? '오늘 마감' : `D-${days}`
}

/** 한국 시간(UTC+9) 기준 그날 0시의 타임스탬프 */
function kstDayStart(date: Date): number {
  const kstOffset = 9 * 60 * 60 * 1000
  const kst = date.getTime() + kstOffset
  return kst - (kst % DAY_MS)
}
