// 관리자 폼의 <input type="datetime-local"> ↔ 서버 ISO 시각(+09:00) 변환 — 항상 한국 시간 기준

const KST_OFFSET_MS = 9 * 60 * 60 * 1000

/** "2026-09-13T18:20:00+09:00" → "2026-09-13T18:20" (한국 시간) */
export function toDatetimeLocal(iso: string | null | undefined): string {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  return new Date(date.getTime() + KST_OFFSET_MS).toISOString().slice(0, 16)
}

/** "2026-09-13T18:20" (한국 시간) → "2026-09-13T18:20:00+09:00", 빈 값은 null */
export function fromDatetimeLocal(value: string): string | null {
  if (!value) return null
  return `${value.length === 16 ? `${value}:00` : value}+09:00`
}

/** 지금 한국 시간을 datetime-local 형식으로 */
export function nowDatetimeLocal(now: Date = new Date()): string {
  return toDatetimeLocal(now.toISOString())
}
