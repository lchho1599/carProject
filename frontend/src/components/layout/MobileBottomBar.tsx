import { NavLink } from 'react-router'
import { cn } from '../../lib/cn'
import { useConsultModal } from '../lead/consultModalContext'
import { CalculatorIcon, CarIcon, ChatIcon, TagIcon } from '../ui/icons'

const itemClass = 'flex flex-col items-center justify-center gap-0.5 text-xs font-semibold'

/** 모바일 하단 고정 메뉴 (md 이상에서는 숨김) */
export function MobileBottomBar() {
  const { openConsult } = useConsultModal()

  return (
    <nav
      aria-label="하단 메뉴"
      className="fixed inset-x-0 bottom-0 z-40 grid h-bottom-bar grid-cols-4 border-t border-gray-200 bg-white pb-[env(safe-area-inset-bottom)] md:hidden"
    >
      <NavLink to="/estimate" className={({ isActive }) => cn(itemClass, isActive ? 'text-primary' : 'text-gray-500')}>
        <CalculatorIcon className="size-6" />
        간편견적
      </NavLink>
      <NavLink to="/deals?type=time" className={({ isActive }) => cn(itemClass, isActive ? 'text-primary' : 'text-gray-500')}>
        <TagIcon className="size-6" />
        특가
      </NavLink>
      <NavLink to="/instant" className={({ isActive }) => cn(itemClass, isActive ? 'text-primary' : 'text-gray-500')}>
        <CarIcon className="size-6" />
        즉시출고
      </NavLink>
      <button type="button" onClick={() => openConsult()} className={cn(itemClass, 'bg-accent text-white')}>
        <ChatIcon className="size-6" />
        상담신청
      </button>
    </nav>
  )
}
