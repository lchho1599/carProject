import { useSearchParams } from 'react-router'
import { DealCard } from '../../components/deal/DealCard'
import { FilterChip } from '../../components/ui/FilterChip'
import { CardSkeletonGrid, EmptyState, ErrorState } from '../../components/ui/StateViews'
import { dealTypeByParam, dealTypeLabel, useDeals, type DealType } from '../../features/deal/api'

const TABS: { param: string; type: DealType; description: string }[] = [
  { param: 'time', type: 'TIME_SALE', description: '기간 한정으로 진행하는 특별 월 납입료' },
  { param: 'nodeposit', type: 'NO_DEPOSIT', description: '보증금·선납금 없이 월 납입료만 내는 상품' },
]

/** 특가 목록 — 주소 ?type=time | nodeposit 로 탭 유지 (광고 링크를 탭에 바로 연결할 수 있음) */
export function DealsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const param = searchParams.get('type') ?? 'time'
  const type = dealTypeByParam[param] ?? 'TIME_SALE'
  const activeTab = TABS.find((tab) => tab.type === type)!
  const { data, isPending, isError, refetch } = useDeals(type)

  return (
    <div className="mx-auto max-w-6xl px-4 py-6 md:py-10">
      <h1 className="text-2xl font-extrabold md:text-3xl">특가</h1>

      <div role="tablist" aria-label="특가 유형" className="mt-4 flex gap-2">
        {TABS.map((tab) => (
          <FilterChip
            key={tab.param}
            selected={tab.type === type}
            onClick={() => setSearchParams({ type: tab.param }, { replace: true })}
          >
            {dealTypeLabel[tab.type]}
          </FilterChip>
        ))}
      </div>
      <p className="mt-3 text-sm text-gray-500">{activeTab.description}</p>

      <div className="mt-6">
        {isPending ? (
          <CardSkeletonGrid count={8} />
        ) : isError ? (
          <ErrorState onRetry={() => refetch()} />
        ) : data.length === 0 ? (
          <EmptyState message="현재 진행 중인 특가가 없습니다. 상담을 신청하시면 비공개 특가를 안내해 드립니다." />
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {data.map((deal) => (
              <DealCard key={deal.id} deal={deal} />
            ))}
          </div>
        )}
      </div>

      <p className="mt-8 text-xs text-gray-400">
        ※ 표시된 월 납입료는 명시된 조건 기준이며, 신용도·보험 조건 등에 따라 달라질 수 있습니다.
      </p>
    </div>
  )
}
