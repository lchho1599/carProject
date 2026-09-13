import { Link } from 'react-router'
import { StatusBadge } from '../../components/admin/StatusBadge'
import { ErrorState } from '../../components/ui/StateViews'
import { Spinner } from '../../components/ui/Spinner'
import { useDashboard } from '../../features/admin/leads'
import { LEAD_STATUSES, leadStatusLabel, leadTypeLabel } from '../../features/lead/labels'
import { formatDateTime } from '../../lib/format'

export function DashboardPage() {
  const { data, isPending, isError, refetch } = useDashboard()

  if (isPending) {
    return (
      <div className="flex justify-center py-20 text-primary">
        <Spinner className="size-8" />
      </div>
    )
  }
  if (isError) return <ErrorState onRetry={() => refetch()} />

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-extrabold">대시보드</h1>

      <section aria-label="기간별 신청" className="grid grid-cols-2 gap-3 md:grid-cols-4">
        <StatCard label="오늘 신청" value={data.todayCount} highlight />
        <StatCard label="이번 주" value={data.weekCount} />
        <StatCard label="이번 달" value={data.monthCount} />
        <StatCard label="전체" value={data.totalCount} />
      </section>

      <section aria-label="상태별 건수" className="rounded-xl bg-white p-4 md:p-5">
        <h2 className="mb-3 font-bold">상태별 건수</h2>
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-5">
          {LEAD_STATUSES.map((status) => (
            <Link
              key={status}
              to={`/admin/leads?status=${status}`}
              className="flex items-center justify-between rounded-lg border border-gray-100 px-3 py-2.5 hover:border-primary-200"
            >
              <StatusBadge status={status} />
              <span className="text-lg font-extrabold">
                {data.statusCounts[status] ?? 0}
                <span className="sr-only">건 {leadStatusLabel[status]}</span>
              </span>
            </Link>
          ))}
        </div>
      </section>

      <section aria-label="최근 신청" className="rounded-xl bg-white p-4 md:p-5">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-bold">최근 신청</h2>
          <Link to="/admin/leads" className="text-sm font-semibold text-primary hover:underline">
            전체보기 ›
          </Link>
        </div>
        {data.recentLeads.length === 0 ? (
          <p className="py-6 text-center text-gray-500">아직 신청이 없습니다.</p>
        ) : (
          <ul className="divide-y divide-gray-100">
            {data.recentLeads.map((lead) => (
              <li key={lead.id}>
                <Link to={`/admin/leads/${lead.id}`} className="flex items-center gap-3 py-3 hover:bg-gray-50">
                  <StatusBadge status={lead.status} />
                  <span className="w-16 shrink-0 text-sm text-gray-500">{leadTypeLabel[lead.type]}</span>
                  <span className="min-w-0 flex-1 truncate font-semibold">
                    {lead.maskedName} · {lead.vehicleName}
                  </span>
                  <span className="hidden text-sm text-gray-500 sm:inline">{formatDateTime(lead.createdAt)}</span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}

function StatCard({ label, value, highlight }: { label: string; value: number; highlight?: boolean }) {
  return (
    <div className={highlight ? 'rounded-xl bg-primary p-4 text-white' : 'rounded-xl bg-white p-4'}>
      <p className={highlight ? 'text-sm text-primary-100' : 'text-sm text-gray-500'}>{label}</p>
      <p className="mt-1 text-3xl font-extrabold">
        {value}
        <span className="ml-1 text-base font-semibold">건</span>
      </p>
    </div>
  )
}
