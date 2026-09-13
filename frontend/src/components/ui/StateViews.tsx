import { cn } from '../../lib/cn'
import { Button } from './Button'

/** 카드 목록 로딩 자리표시 */
export function CardSkeletonGrid({ count = 4, className }: { count?: number; className?: string }) {
  return (
    <div className={cn('grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4', className)} aria-busy="true" aria-label="불러오는 중">
      {Array.from({ length: count }, (_, index) => (
        <div key={index} className="animate-pulse overflow-hidden rounded-xl border border-gray-100 bg-white">
          <div className="aspect-[16/10] bg-gray-100" />
          <div className="space-y-2 p-4">
            <div className="h-4 w-2/3 rounded bg-gray-100" />
            <div className="h-3 w-full rounded bg-gray-100" />
            <div className="h-6 w-1/2 rounded bg-gray-100" />
          </div>
        </div>
      ))}
    </div>
  )
}

export function ErrorState({ message = '정보를 불러오지 못했습니다.', onRetry }: { message?: string; onRetry?: () => void }) {
  return (
    <div role="alert" className="rounded-xl border border-gray-200 bg-white px-4 py-10 text-center">
      <p className="text-gray-600">{message}</p>
      {onRetry && (
        <Button variant="outline" size="sm" className="mt-4" onClick={onRetry}>
          다시 시도
        </Button>
      )}
    </div>
  )
}

export function EmptyState({ message }: { message: string }) {
  return <div className="rounded-xl border border-dashed border-gray-300 bg-white px-4 py-10 text-center text-gray-500">{message}</div>
}
