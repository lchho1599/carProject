import { createContext, useContext } from 'react'
import type { LeadContext } from '../../features/lead/types'

export type ConsultModalApi = {
  /** 상담 모달 열기 — 인자를 생략하면 빠른상담(QUICK) */
  openConsult: (context?: LeadContext) => void
}

export const ConsultModalContext = createContext<ConsultModalApi | null>(null)

export function useConsultModal(): ConsultModalApi {
  const api = useContext(ConsultModalContext)
  if (!api) throw new Error('useConsultModal은 ConsultModalProvider 안에서만 사용할 수 있습니다.')
  return api
}
