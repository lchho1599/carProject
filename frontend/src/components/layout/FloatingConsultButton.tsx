import { useConsultModal } from '../lead/consultModalContext'
import { ChatIcon } from '../ui/icons'

/** PC 우측 하단 상담 버튼 (모바일은 하단 바가 대신함) */
export function FloatingConsultButton() {
  const { openConsult } = useConsultModal()

  return (
    <button
      type="button"
      onClick={() => openConsult()}
      className="fixed right-6 bottom-6 z-30 hidden items-center gap-2 rounded-full bg-accent px-5 py-3.5 font-bold text-white shadow-lg transition-colors hover:bg-accent-600 md:flex"
    >
      <ChatIcon className="size-5" />
      무료 상담
    </button>
  )
}
