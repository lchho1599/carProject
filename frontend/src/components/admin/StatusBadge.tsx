import { leadStatusLabel, leadStatusTone, type LeadStatus } from '../../features/lead/labels'
import { cn } from '../../lib/cn'

export function StatusBadge({ status, className }: { status: LeadStatus; className?: string }) {
  return (
    <span className={cn('inline-flex rounded-full px-2.5 py-0.5 text-xs font-bold whitespace-nowrap', leadStatusTone[status], className)}>
      {leadStatusLabel[status]}
    </span>
  )
}
