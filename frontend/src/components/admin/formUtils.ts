import { ApiError } from '../../lib/apiClient'

// 관리자 폼 공통 함수 (컴포넌트는 form.tsx)

export const adminInputClass =
  'h-10 w-full rounded-lg border bg-white px-3 text-sm focus:border-primary focus:outline-none disabled:bg-gray-50'

/** ApiError 의 fieldErrors 를 { 필드명: 메시지 } 로 */
export function fieldErrorMap(error: unknown): Record<string, string> {
  if (!(error instanceof ApiError)) return {}
  return Object.fromEntries(error.fieldErrors.map((fieldError) => [fieldError.field, fieldError.message]))
}

/** 항목 오류가 아닌 전체 오류 메시지 (항목 오류만 있으면 안내 문구) */
export function formErrorMessage(error: unknown): string | null {
  if (!error) return null
  if (error instanceof ApiError) return error.fieldErrors.length > 0 ? '입력값을 확인해 주세요.' : error.message
  return '저장하지 못했습니다. 잠시 후 다시 시도해 주세요.'
}

/** 숫자 입력칸의 문자열 → 숫자 (빈 값·잘못된 값은 null, 쉼표 허용) */
export function toNumberOrNull(value: string): number | null {
  if (value.trim() === '') return null
  const number = Number(value.replaceAll(',', ''))
  return Number.isFinite(number) ? number : null
}
