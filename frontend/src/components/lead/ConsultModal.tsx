import { useCallback, useMemo, useState, type ReactNode } from 'react'
import type { LeadContext } from '../../features/lead/types'
import { Modal } from '../ui/Modal'
import { ConsultModalContext } from './consultModalContext'
import { LeadForm } from './LeadForm'

const QUICK_CONTEXT: LeadContext = { type: 'QUICK' }

/** 어느 화면에서나 useConsultModal().openConsult(...)로 상담 모달을 띄울 수 있게 한다 */
export function ConsultModalProvider({ children }: { children: ReactNode }) {
  const [context, setContext] = useState<LeadContext | null>(null)
  // 열 때마다 폼을 새로 만들기 위한 key
  const [openCount, setOpenCount] = useState(0)

  const openConsult = useCallback((next?: LeadContext) => {
    setContext(next ?? QUICK_CONTEXT)
    setOpenCount((count) => count + 1)
  }, [])
  const close = useCallback(() => setContext(null), [])
  const api = useMemo(() => ({ openConsult }), [openConsult])

  return (
    <ConsultModalContext.Provider value={api}>
      {children}
      <Modal open={context !== null} onClose={close} title="무료 상담 신청">
        {context && (
          <>
            {context.label ? (
              <p className="mb-4 rounded-lg bg-primary-50 px-3 py-2 text-sm font-semibold text-primary-700">
                {context.label}
              </p>
            ) : (
              <p className="mb-4 text-sm text-gray-600">
                연락처를 남겨 주시면 전문 상담사가 조건에 맞는 최저가 견적을 안내해 드립니다.
              </p>
            )}
            <LeadForm key={openCount} context={context} onSuccess={close} />
          </>
        )}
      </Modal>
    </ConsultModalContext.Provider>
  )
}
