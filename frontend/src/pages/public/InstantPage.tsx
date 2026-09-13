import { useSearchParams } from 'react-router'
import { InstantStockCard } from '../../components/instant/InstantStockCard'
import { FilterChip } from '../../components/ui/FilterChip'
import { CardSkeletonGrid, EmptyState, ErrorState } from '../../components/ui/StateViews'
import { useInstantStocks, type InstantFilter } from '../../features/instant/api'
import { useBrands } from '../../features/vehicle/api'

/** 차종 필터 — 전기차는 연료(EV) 기준 */
const KIND_FILTERS: { key: string; label: string; filter: InstantFilter }[] = [
  { key: 'all', label: '전체', filter: {} },
  { key: 'suv', label: 'SUV', filter: { bodyType: 'SUV' } },
  { key: 'sedan', label: '세단', filter: { bodyType: 'SEDAN' } },
  { key: 'van', label: '승합', filter: { bodyType: 'VAN' } },
  { key: 'ev', label: '전기차', filter: { fuel: 'EV' } },
]

/**
 * 즉시출고 목록 — 브랜드 탭(전체 / 국산 브랜드 / 수입) + 차종 필터.
 * 선택값은 주소(?brand=3&kind=ev)에 남겨 공유·뒤로가기 시 유지한다.
 */
export function InstantPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const brandParam = searchParams.get('brand') ?? 'all'
  const kindParam = searchParams.get('kind') ?? 'all'

  const domesticBrands = useBrands('DOMESTIC')
  const kind = KIND_FILTERS.find((item) => item.key === kindParam) ?? KIND_FILTERS[0]

  const filter: InstantFilter = { ...kind.filter }
  if (brandParam === 'imported') filter.origin = 'IMPORTED'
  else if (brandParam !== 'all' && Number.isFinite(Number(brandParam))) filter.brandId = Number(brandParam)

  const { data, isPending, isError, isFetching, refetch } = useInstantStocks(filter)

  const updateParam = (key: 'brand' | 'kind', value: string) => {
    const next = new URLSearchParams(searchParams)
    if (value === 'all') next.delete(key)
    else next.set(key, value)
    setSearchParams(next, { replace: true })
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-6 md:py-10">
      <h1 className="text-2xl font-extrabold md:text-3xl">즉시출고</h1>
      <p className="mt-2 text-gray-500">기다림 없이 빠르게 인도받을 수 있는 재고 차량입니다.</p>

      <div className="mt-5 space-y-3">
        <div role="group" aria-label="브랜드" className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1 md:mx-0 md:flex-wrap md:px-0">
          <FilterChip selected={brandParam === 'all'} onClick={() => updateParam('brand', 'all')}>
            전체
          </FilterChip>
          {domesticBrands.data?.map((brand) => (
            <FilterChip
              key={brand.id}
              selected={brandParam === String(brand.id)}
              onClick={() => updateParam('brand', String(brand.id))}
            >
              {brand.name}
            </FilterChip>
          ))}
          <FilterChip selected={brandParam === 'imported'} onClick={() => updateParam('brand', 'imported')}>
            수입
          </FilterChip>
        </div>

        <div role="group" aria-label="차종" className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1 md:mx-0 md:px-0">
          {KIND_FILTERS.map((item) => (
            <FilterChip key={item.key} selected={kind.key === item.key} onClick={() => updateParam('kind', item.key)}>
              {item.label}
            </FilterChip>
          ))}
        </div>
      </div>

      <p className="mt-5 text-sm text-gray-600" aria-live="polite">
        {data ? (
          <>
            총 <strong className="text-accent-600">{data.totalCount}대</strong>의 즉시출고 차량 대기중
          </>
        ) : (
          ' '
        )}
      </p>
      <p className="mt-1 text-xs text-gray-400">※ 재고는 실시간으로 변동되며, 세부 사항은 상담을 통해 확인해 주세요.</p>

      <div className="mt-4">
        {isPending ? (
          <CardSkeletonGrid count={8} />
        ) : isError ? (
          <ErrorState onRetry={() => refetch()} />
        ) : data.items.length === 0 ? (
          <EmptyState message="조건에 맞는 즉시출고 차량이 없습니다. 다른 조건을 선택하거나 상담을 신청해 주세요." />
        ) : (
          <div className={`grid grid-cols-1 gap-4 transition-opacity sm:grid-cols-2 lg:grid-cols-4 ${isFetching ? 'opacity-60' : ''}`}>
            {data.items.map((stock) => (
              <InstantStockCard key={stock.id} stock={stock} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
