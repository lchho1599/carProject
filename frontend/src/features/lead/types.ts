// 백엔드 LeadCreateRequest / LeadConditions 와 같은 형식

export type LeadType = 'QUICK' | 'ESTIMATE' | 'DEAL' | 'INSTANT'

export type LeadConditions = {
  useType?: 'RENT' | 'LEASE'
  periodMonths?: number
  depositRate?: number
  prepayRate?: number
  insuranceAge?: 21 | 26
  /** 10000 / 20000 / 30000, 0 = 무제한 */
  annualMileage?: number
  creditScore?: 'UNDER_700' | 'OVER_700' | 'UNKNOWN'
}

/** 상담 신청 대상 — 화면(특가 카드, 즉시출고 카드, 간편견적)이 폼에 넘겨주는 정보 */
export type LeadContext = {
  type: LeadType
  /** 모달 상단에 보여줄 차량·상품 이름 (예: "기아 쏘렌토 하이브리드") */
  label?: string
  brandId?: number
  modelId?: number
  trimId?: number
  colorId?: number
  optionIds?: number[]
  dealId?: number
  instantStockId?: number
  conditions?: LeadConditions
}

export type LeadCreateRequest = Omit<LeadContext, 'label'> & {
  name: string
  phone: string
  agreePrivacy: boolean
  agreeMarketing: boolean
  sourceUrl?: string
  utm?: Record<string, string>
  /** 스팸 방지 숨김 필드 — 사람은 항상 빈 값 */
  website?: string
}

export type LeadCreateResponse = { id: number | null }
