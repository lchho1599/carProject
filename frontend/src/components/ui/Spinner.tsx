import { cn } from '../../lib/cn'

export function Spinner({ className }: { className?: string }) {
  return (
    <span
      role="status"
      aria-label="처리 중"
      className={cn('inline-block animate-spin rounded-full border-2 border-current border-r-transparent', className)}
    />
  )
}
