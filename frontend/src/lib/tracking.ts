import type { LeadType } from '../features/lead/types'

declare global {
  interface Window {
    dataLayer?: unknown[]
  }
}

/**
 * 상담 신청 완료 전환 이벤트 — 광고 스크립트(GA4, 구글·네이버·메타)는 추후 연동한다.
 * 지금은 dataLayer 가 있을 때만 이벤트를 넣는다.
 */
export function trackLeadSubmitted(type: LeadType): void {
  window.dataLayer?.push({ event: 'lead_submitted', leadType: type })
}
