import type { LeadType } from './types'

export type LeadStatus = 'NEW' | 'IN_PROGRESS' | 'CONTRACTED' | 'NO_ANSWER' | 'CANCELED'

export const LEAD_TYPES: LeadType[] = ['QUICK', 'ESTIMATE', 'DEAL', 'INSTANT']
export const LEAD_STATUSES: LeadStatus[] = ['NEW', 'IN_PROGRESS', 'CONTRACTED', 'NO_ANSWER', 'CANCELED']

export const leadTypeLabel: Record<LeadType, string> = {
  QUICK: '빠른상담',
  ESTIMATE: '간편견적',
  DEAL: '특가',
  INSTANT: '즉시출고',
}

export const leadStatusLabel: Record<LeadStatus, string> = {
  NEW: '신규',
  IN_PROGRESS: '상담중',
  CONTRACTED: '계약완료',
  NO_ANSWER: '부재',
  CANCELED: '취소',
}

/** 상태 배지 색 (Tailwind 클래스) */
export const leadStatusTone: Record<LeadStatus, string> = {
  NEW: 'bg-accent-50 text-accent-700',
  IN_PROGRESS: 'bg-primary-50 text-primary-700',
  CONTRACTED: 'bg-green-50 text-green-700',
  NO_ANSWER: 'bg-amber-50 text-amber-700',
  CANCELED: 'bg-gray-100 text-gray-500',
}
