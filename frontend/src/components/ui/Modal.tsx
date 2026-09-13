import { useEffect, useId, useRef, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { cn } from '../../lib/cn'

type Props = {
  open: boolean
  onClose: () => void
  title: string
  children: ReactNode
  /** 모바일에서 하단 시트로 표시 (기본 true) */
  sheetOnMobile?: boolean
  className?: string
}

/**
 * 공통 모달 — 모바일은 하단 시트, PC는 가운데 창.
 * ESC·배경 클릭으로 닫기, 열려 있는 동안 배경 스크롤 잠금, 닫으면 이전 포커스로 복귀.
 */
export function Modal({ open, onClose, title, children, sheetOnMobile = true, className }: Props) {
  const titleId = useId()
  const panelRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const previousFocus = document.activeElement as HTMLElement | null
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    panelRef.current?.focus()

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      document.body.style.overflow = previousOverflow
      previousFocus?.focus?.()
    }
  }, [open, onClose])

  if (!open) return null

  return createPortal(
    <div
      className={cn(
        'fixed inset-0 z-50 flex justify-center bg-black/50 p-0',
        sheetOnMobile ? 'items-end md:items-center md:p-4' : 'items-center p-4',
      )}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
        className={cn(
          'flex max-h-[90dvh] w-full flex-col bg-white shadow-xl outline-none',
          sheetOnMobile ? 'rounded-t-2xl md:max-w-md md:rounded-2xl' : 'max-w-md rounded-2xl',
          className,
        )}
      >
        <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4">
          <h2 id={titleId} className="text-lg font-bold">
            {title}
          </h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="닫기"
            className="-mr-2 flex size-9 items-center justify-center rounded-full text-2xl leading-none text-gray-500 hover:bg-gray-100"
          >
            ×
          </button>
        </div>
        <div className="overflow-y-auto px-5 py-5 pb-[max(1.25rem,env(safe-area-inset-bottom))]">{children}</div>
      </div>
    </div>,
    document.body,
  )
}
