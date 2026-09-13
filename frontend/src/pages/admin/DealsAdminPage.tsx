import { Link, useNavigate, useSearchParams } from 'react-router'
import { AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { DisplayStatusBadge } from '../../components/admin/DisplayStatusBadge'
import { FilterChip } from '../../components/ui/FilterChip'
import { ErrorState } from '../../components/ui/StateViews'
import { useAdminDeals } from '../../features/admin/catalog'
import { dealTypeLabel, type DealType } from '../../features/deal/api'
import { formatDateTime, formatWon } from '../../lib/format'

export function DealsAdminPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const typeParam = searchParams.get('type')
  const type = typeParam === 'TIME_SALE' || typeParam === 'NO_DEPOSIT' ? (typeParam as DealType) : undefined
  const { data, isPending, isError, refetch } = useAdminDeals(type)

  return (
    <div className="space-y-4">
      <AdminPageHeader
        title="특가 관리"
        actions={
          <Link to="/admin/deals/new" className="inline-flex h-9 items-center rounded-lg bg-primary px-3 text-sm font-semibold text-white hover:bg-primary-800">
            특가 등록
          </Link>
        }
      />
      <div role="tablist" aria-label="특가 유형" className="flex gap-2">
        <FilterChip selected={!type} onClick={() => setSearchParams({}, { replace: true })}>
          전체
        </FilterChip>
        {(['TIME_SALE', 'NO_DEPOSIT'] as DealType[]).map((value) => (
          <FilterChip key={value} selected={type === value} onClick={() => setSearchParams({ type: value }, { replace: true })}>
            {dealTypeLabel[value]}
          </FilterChip>
        ))}
      </div>

      {isPending ? (
        <AdminLoading />
      ) : isError ? (
        <ErrorState onRetry={() => refetch()} />
      ) : data.length === 0 ? (
        <div className="rounded-xl bg-white py-12 text-center text-gray-500">등록된 특가가 없습니다.</div>
      ) : (
        <div className="overflow-x-auto rounded-xl bg-white">
          <table className="w-full min-w-[860px] text-sm">
            <thead className="bg-gray-50 text-left text-gray-500">
              <tr>
                <th className="px-4 py-3 font-semibold">상태</th>
                <th className="px-4 py-3 font-semibold">유형</th>
                <th className="px-4 py-3 font-semibold">제목 · 차량</th>
                <th className="px-4 py-3 text-right font-semibold">월 납입료</th>
                <th className="px-4 py-3 font-semibold">조건</th>
                <th className="px-4 py-3 font-semibold">노출 기간</th>
                <th className="px-4 py-3 text-right font-semibold">순서</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {data.map((deal) => (
                <tr key={deal.id} onClick={() => navigate(`/admin/deals/${deal.id}`)} className="cursor-pointer hover:bg-primary-50/40">
                  <td className="px-4 py-3">
                    <DisplayStatusBadge status={deal.displayStatus} />
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">{dealTypeLabel[deal.type]}</td>
                  <td className="px-4 py-3">
                    <Link to={`/admin/deals/${deal.id}`} onClick={(e) => e.stopPropagation()} className="font-semibold hover:underline">
                      {deal.title}
                    </Link>
                    <span className="block text-xs text-gray-500">
                      {deal.vehicle.brandName} {deal.vehicle.modelName} · {deal.vehicle.trimName}
                      {!deal.vehicleActive && <span className="ml-1 text-amber-700">(차량 사용 안 함)</span>}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right whitespace-nowrap">
                    {deal.originalMonthly !== null && <del className="block text-xs text-gray-400">{formatWon(deal.originalMonthly)}</del>}
                    <strong>{formatWon(deal.monthlyPrice)}</strong>
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-gray-600">
                    {deal.periodMonths}개월 · 보증 {deal.depositRate}% · 선납 {deal.prepayRate}%
                  </td>
                  <td className="px-4 py-3 text-xs whitespace-nowrap text-gray-600">
                    {formatDateTime(deal.startsAt)}
                    <br />~ {deal.endsAt ? formatDateTime(deal.endsAt) : '종료일 없음'}
                  </td>
                  <td className="px-4 py-3 text-right">{deal.sortOrder}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
