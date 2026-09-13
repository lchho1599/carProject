import type { DisplayStatus } from '../../features/admin/catalog'
import { displayStatusLabel } from '../../features/admin/labels'
import { cn } from '../../lib/cn'

const tone: Record<DisplayStatus, string> = {
  VISIBLE: 'bg-green-50 text-green-700',
  SCHEDULED: 'bg-primary-50 text-primary-700',
  ENDED: 'bg-gray-100 text-gray-500',
  HIDDEN: 'bg-amber-50 text-amber-700',
}

export function DisplayStatusBadge({ status }: { status: DisplayStatus }) {
  return (
    <span className={cn('inline-flex rounded-full px-2.5 py-0.5 text-xs font-bold whitespace-nowrap', tone[status])}>
      {displayStatusLabel[status]}
    </span>
  )
}

/** 사용 여부 배지 (차량 마스터) */
export function ActiveBadge({ active }: { active: boolean }) {
  return (
    <span
      className={cn(
        'inline-flex rounded-full px-2 py-0.5 text-xs font-bold whitespace-nowrap',
        active ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500',
      )}
    >
      {active ? '사용' : '사용 안 함'}
    </span>
  )
}
