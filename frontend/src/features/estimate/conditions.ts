import type { LeadConditions } from '../lead/types'

// 간편견적 2단계 이용조건 선택지 (백엔드 LeadConditions 값과 같은 형식)

export type Choice<T> = { value: T; label: string }

export const USE_TYPES: Choice<NonNullable<LeadConditions['useType']>>[] = [
  { value: 'RENT', label: '장기렌트' },
  { value: 'LEASE', label: '리스' },
]

export const PERIODS: Choice<number>[] = [
  { value: 36, label: '36개월' },
  { value: 48, label: '48개월' },
  { value: 60, label: '60개월' },
]

export const RATES: Choice<number>[] = [0, 10, 20, 30, 40].map((rate) => ({
  value: rate,
  label: rate === 0 ? '없음' : `${rate}%`,
}))

export const INSURANCE_AGES: Choice<NonNullable<LeadConditions['insuranceAge']>>[] = [
  { value: 21, label: '만 21세 이상' },
  { value: 26, label: '만 26세 이상' },
]

/** 0 = 무제한 */
export const MILEAGES: Choice<number>[] = [
  { value: 10000, label: '연 1만km' },
  { value: 20000, label: '연 2만km' },
  { value: 30000, label: '연 3만km' },
  { value: 0, label: '무제한' },
]

export const CREDIT_SCORES: Choice<NonNullable<LeadConditions['creditScore']>>[] = [
  { value: 'OVER_700', label: '700점 이상' },
  { value: 'UNDER_700', label: '700점 미만' },
  { value: 'UNKNOWN', label: '잘 모르겠어요' },
]

/** 처음 화면에 선택되어 있는 조건 — 특가 카드 기준 조건(48개월·선납 30%)과 맞춘다 */
export const DEFAULT_CONDITIONS: Required<LeadConditions> = {
  useType: 'RENT',
  periodMonths: 48,
  depositRate: 0,
  prepayRate: 30,
  insuranceAge: 26,
  annualMileage: 20000,
  creditScore: 'UNKNOWN',
}
