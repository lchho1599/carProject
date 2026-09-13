import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { ApiError } from '../../lib/apiClient'
import { ErrorState } from '../ui/StateViews'
import { Spinner } from '../ui/Spinner'

export function AdminLoading() {
  return (
    <div className="flex justify-center py-20 text-primary">
      <Spinner className="size-8" />
    </div>
  )
}

export function AdminLoadError({ error, onRetry, backTo }: { error: unknown; onRetry: () => void; backTo: string }) {
  const notFound = error instanceof ApiError && error.status === 404
  return (
    <div className="space-y-4">
      <Link to={backTo} className="text-sm font-semibold text-gray-600 hover:text-primary">
        ‹ 목록으로
      </Link>
      <ErrorState message={notFound ? '찾을 수 없습니다. 이미 삭제되었을 수 있습니다.' : undefined} onRetry={notFound ? undefined : onRetry} />
    </div>
  )
}

export function AdminPageHeader({ title, backTo, actions }: { title: string; backTo?: string; actions?: ReactNode }) {
  return (
    <div className="space-y-2">
      {backTo && (
        <Link to={backTo} className="inline-block text-sm font-semibold text-gray-600 hover:text-primary">
          ‹ 목록으로
        </Link>
      )}
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-extrabold">{title}</h1>
        {actions && <div className="flex gap-2">{actions}</div>}
      </div>
    </div>
  )
}
