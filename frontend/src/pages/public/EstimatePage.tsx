import { Link, useSearchParams } from 'react-router'
import { FilterChip } from '../../components/ui/FilterChip'
import { CardSkeletonGrid, EmptyState, ErrorState } from '../../components/ui/StateViews'
import { VehicleImage } from '../../components/vehicle/VehicleImage'
import { useBrands, useModels } from '../../features/vehicle/api'
import type { Origin } from '../../features/vehicle/types'
import { cn } from '../../lib/cn'
import { formatManwon } from '../../lib/format'

const ORIGIN_TABS: { value: Origin; label: string }[] = [
  { value: 'DOMESTIC', label: '국산차' },
  { value: 'IMPORTED', label: '수입차' },
]

/**
 * 간편견적 1단계 — 국산/수입 → 브랜드 → 모델 선택.
 * 선택 상태는 주소(?origin=IMPORTED&brand=5)에 남겨 뒤로가기 시 유지한다.
 */
export function EstimatePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const origin: Origin = searchParams.get('origin') === 'IMPORTED' ? 'IMPORTED' : 'DOMESTIC'
  const brandParam = Number(searchParams.get('brand'))
  const brands = useBrands(origin)
  const selectedBrand = brands.data?.find((brand) => brand.id === brandParam)
  const models = useModels(selectedBrand?.id)

  const selectOrigin = (next: Origin) => setSearchParams(next === 'DOMESTIC' ? {} : { origin: next }, { replace: true })
  const selectBrand = (brandId: number) => {
    const next = new URLSearchParams(searchParams)
    next.set('brand', String(brandId))
    setSearchParams(next, { replace: true })
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-6 md:py-10">
      <StepHeader step={1} title="차량 선택" description="견적을 받아볼 브랜드와 모델을 골라 주세요." />

      <div role="tablist" aria-label="국산·수입" className="mt-6 flex gap-2">
        {ORIGIN_TABS.map((tab) => (
          <FilterChip key={tab.value} selected={origin === tab.value} onClick={() => selectOrigin(tab.value)}>
            {tab.label}
          </FilterChip>
        ))}
      </div>

      <section aria-label="브랜드" className="mt-4">
        {brands.isPending ? (
          <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-6">
            {Array.from({ length: 6 }, (_, index) => (
              <div key={index} className="h-20 animate-pulse rounded-xl bg-gray-100" />
            ))}
          </div>
        ) : brands.isError ? (
          <ErrorState onRetry={() => brands.refetch()} />
        ) : brands.data.length === 0 ? (
          <EmptyState message="선택할 수 있는 브랜드가 없습니다." />
        ) : (
          <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-6">
            {brands.data.map((brand) => (
              <button
                key={brand.id}
                type="button"
                aria-pressed={selectedBrand?.id === brand.id}
                onClick={() => selectBrand(brand.id)}
                className={cn(
                  'flex h-20 flex-col items-center justify-center gap-1 rounded-xl border bg-white font-bold transition-colors',
                  selectedBrand?.id === brand.id
                    ? 'border-primary text-primary ring-2 ring-primary-100'
                    : 'border-gray-200 text-gray-700 hover:border-primary-300',
                )}
              >
                {brand.logoUrl ? (
                  <img src={brand.logoUrl} alt="" className="h-8 object-contain" />
                ) : (
                  <span aria-hidden="true" className="flex size-8 items-center justify-center rounded-full bg-primary-50 text-sm text-primary">
                    {brand.name.slice(0, 1)}
                  </span>
                )}
                <span className="text-sm">{brand.name}</span>
              </button>
            ))}
          </div>
        )}
      </section>

      <section aria-label="모델" className="mt-8">
        {!selectedBrand ? (
          <p className="rounded-xl border border-dashed border-gray-300 bg-white px-4 py-10 text-center text-gray-500">
            위에서 브랜드를 선택하면 모델이 나타납니다.
          </p>
        ) : (
          <>
            <h2 className="mb-3 text-lg font-extrabold">{selectedBrand.name} 모델</h2>
            {models.isPending ? (
              <CardSkeletonGrid count={3} className="lg:grid-cols-3" />
            ) : models.isError ? (
              <ErrorState onRetry={() => models.refetch()} />
            ) : models.data.length === 0 ? (
              <EmptyState message="현재 견적 가능한 모델이 없습니다. 상담을 신청해 주세요." />
            ) : (
              <ul className="grid grid-cols-2 gap-3 md:grid-cols-3">
                {models.data.map((model) => (
                  <li key={model.id}>
                    <Link
                      to={`/estimate/${model.id}`}
                      className="block overflow-hidden rounded-xl border border-gray-200 bg-white transition-colors hover:border-primary"
                    >
                      <VehicleImage src={model.imageUrl} alt={`${selectedBrand.name} ${model.name}`} />
                      <div className="p-3">
                        <p className="font-bold">{model.name}</p>
                        <p className="mt-0.5 text-xs text-gray-500">{model.segment}</p>
                        {model.minPrice !== null && (
                          <p className="mt-1 text-sm font-semibold text-primary">{formatManwon(model.minPrice)}~</p>
                        )}
                      </div>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </>
        )}
      </section>
    </div>
  )
}

export function StepHeader({ step, title, description }: { step: 1 | 2; title: string; description: string }) {
  return (
    <div>
      <ol aria-label="견적 단계" className="mb-3 flex items-center gap-2 text-sm font-semibold">
        {[
          { number: 1, label: '차량 선택' },
          { number: 2, label: '옵션·조건 선택' },
        ].map((item, index) => (
          <li key={item.number} className="flex items-center gap-2">
            {index > 0 && <span aria-hidden="true" className="text-gray-300">›</span>}
            <span
              aria-current={item.number === step ? 'step' : undefined}
              className={item.number === step ? 'text-primary' : 'text-gray-400'}
            >
              {item.number}. {item.label}
            </span>
          </li>
        ))}
      </ol>
      <h1 className="text-2xl font-extrabold md:text-3xl">간편 견적 · {title}</h1>
      <p className="mt-1 text-gray-500">{description}</p>
    </div>
  )
}
