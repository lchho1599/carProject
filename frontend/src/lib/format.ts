const wonFormatter = new Intl.NumberFormat('ko-KR')

/** 12345678 → "12,345,678원" */
export function formatWon(amount: number): string {
  return `${wonFormatter.format(amount)}원`
}

/** 월 납입료 표기: 209000 → "월 209,000원" */
export function formatMonthly(amount: number): string {
  return `월 ${formatWon(amount)}`
}

/** 차량가 만원 단위: 39900000 → "3,990만원" */
export function formatManwon(amount: number): string {
  return `${wonFormatter.format(Math.round(amount / 10000))}만원`
}

const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone: 'Asia/Seoul',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hourCycle: 'h23',
})

/** ISO 시각 → 한국 시간 "2026-09-13 18:20" */
export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  const parts = Object.fromEntries(dateTimeFormatter.formatToParts(date).map((part) => [part.type, part.value]))
  return `${parts.year}-${parts.month}-${parts.day} ${parts.hour}:${parts.minute}`
}

/** 입력 중인 휴대폰 번호에 하이픈 넣기: "01012345678" → "010-1234-5678" */
export function formatPhoneInput(value: string): string {
  const digits = value.replace(/\D/g, '').slice(0, 11)
  if (digits.length < 4) return digits
  if (digits.length < 8) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  if (digits.length === 10) return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}
