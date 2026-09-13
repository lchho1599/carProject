import { Link, useNavigate, useSearchParams } from 'react-router'
import { AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { FilterChip } from '../../components/ui/FilterChip'
import { ErrorState } from '../../components/ui/StateViews'
import { useAdminInstants } from '../../features/admin/catalog'
import { stockStatusLabel } from '../../features/admin/labels'
import type { StockStatus } from '../../features/instant/api'
import { cn } from '../../lib/cn'
import { formatDateTime, formatManwon, formatWon } from '../../lib/format'

const STATUSES: StockStatus[] = ['AVAILABLE', 'RESERVED', 'SOLD']

export function InstantAdminPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const statusParam = searchParams.get('status') as StockStatus | null
  const status = statusParam && STATUSES.includes(statusParam) ? statusParam : undefined
  const { data, isPending, isError, refetch } = useAdminInstants(status)

  return (
    <div className="space-y-4">
      <AdminPageHeader
        title="즉시출고 관리"
        actions={
          <Link to="/admin/instant/new" className="inline-flex h-9 items-center rounded-lg bg-primary px-3 text-sm font-semibold text-white hover:bg-primary-800">
            재고 등록
          </Link>
        }
      />
      <div role="tablist" aria-label="재고 상태" className="flex gap-2">
        <FilterChip selected={!status} onClick={() => setSearchParams({}, { replace: true })}>
          전체
        </FilterChip>
        {STATUSES.map((value) => (
          <FilterChip key={value} selected={status === value} onClick={() => setSearchParams({ status: value }, { replace: true })}>
            {stockStatusLabel[value]}
          </FilterChip>
        ))}
      </div>

      {isPending ? (
        <AdminLoading />
      ) : isError ? (
        <ErrorState onRetry={() => refetch()} />
      ) : data.length === 0 ? (
        <div className="rounded-xl bg-white py-12 text-center text-gray-500">등록된 재고가 없습니다.</div>
      ) : (
        <div className="overflow-x-auto rounded-xl bg-white">
          <table className="w-full min-w-[820px] text-sm">
            <thead className="bg-gray-50 text-left text-gray-500">
              <tr>
                <th className="px-4 py-3 font-semibold">상태</th>
                <th className="px-4 py-3 font-semibold">차량</th>
                <th className="px-4 py-3 font-semibold">색상</th>
                <th className="px-4 py-3 text-right font-semibold">차량가</th>
                <th className="px-4 py-3 text-right font-semibold">월 납입료</th>
                <th className="px-4 py-3 font-semibold">화면 노출</th>
                <th className="px-4 py-3 font-semibold">수정일</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {data.map((stock) => (
                <tr key={stock.id} onClick={() => navigate(`/admin/instant/${stock.id}`)} className="cursor-pointer hover:bg-primary-50/40">
                  <td className="px-4 py-3">
                    <span
                      className={cn(
                        'inline-flex rounded-full px-2.5 py-0.5 text-xs font-bold',
                        stock.status === 'AVAILABLE' && 'bg-green-50 text-green-700',
                        stock.status === 'RESERVED' && 'bg-primary-50 text-primary-700',
                        stock.status === 'SOLD' && 'bg-gray-100 text-gray-500',
                      )}
                    >
                      {stockStatusLabel[stock.status]}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <Link to={`/admin/instant/${stock.id}`} onClick={(e) => e.stopPropagation()} className="font-semibold hover:underline">
                      {stock.vehicle.brandName} {stock.vehicle.modelName}
                    </Link>
                    <span className="block text-xs text-gray-500">{stock.vehicle.trimName}</span>
                  </td>
                  <td className="px-4 py-3 text-gray-600">
                    {stock.exteriorColor}
                    {stock.interiorColor && ` / ${stock.interiorColor}`}
                  </td>
                  <td className="px-4 py-3 text-right whitespace-nowrap">{formatManwon(stock.vehiclePrice)}</td>
                  <td className="px-4 py-3 text-right font-semibold whitespace-nowrap">{formatWon(stock.monthlyPrice)}</td>
                  <td className="px-4 py-3 whitespace-nowrap">
                    {stock.visible ? (
                      <span className="text-green-700">노출중</span>
                    ) : (
                      <span className="text-gray-500">{!stock.published ? '비공개' : !stock.vehicleActive ? '차량 사용 안 함' : '숨김'}</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-xs whitespace-nowrap text-gray-500">{formatDateTime(stock.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
