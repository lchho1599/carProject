import { DealCard } from '../../components/deal/DealCard'
import { BannerSlider } from '../../components/home/BannerSlider'
import { CardRow, CardRowItem } from '../../components/home/CardRow'
import { QuickEstimateForm } from '../../components/home/QuickEstimateForm'
import { SectionHeader } from '../../components/home/SectionHeader'
import { UsageSteps } from '../../components/home/UsageSteps'
import { InstantStockCard } from '../../components/instant/InstantStockCard'
import { CardSkeletonGrid, EmptyState, ErrorState } from '../../components/ui/StateViews'
import { useBanners } from '../../features/banner/api'
import { useDeals, type DealType } from '../../features/deal/api'
import { useInstantStocks } from '../../features/instant/api'

const HOME_CARD_LIMIT = 8

export function HomePage() {
  return (
    <div className="mx-auto max-w-6xl space-y-12 px-4 py-6 md:space-y-16 md:py-10">
      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
        {/* 왼쪽: 배너 + (PC) 이용 절차 — 오른쪽 폼이 더 길어 생기는 배너 아래 빈 공간을 채운다 */}
        <div className="flex min-w-0 flex-col gap-6">
          <HomeBanners />
          <UsageSteps variant="compact" className="hidden flex-1 lg:flex" />
        </div>
        <section aria-labelledby="quick-estimate-title" className="rounded-2xl border border-gray-200 bg-white p-5">
          <h2 id="quick-estimate-title" className="text-lg font-extrabold">
            빠른 견적 문의
          </h2>
          <p className="mb-4 text-sm text-gray-500">차량을 몰라도 괜찮아요. 연락처만 남겨도 상담해 드립니다.</p>
          <QuickEstimateForm />
        </section>
      </div>

      <DealSection
        type="TIME_SALE"
        title="타임특가"
        description="기간 한정! 핫한 차량을 특별한 월 납입료로"
        moreTo="/deals?type=time"
      />
      <DealSection
        type="NO_DEPOSIT"
        title="무보증특가"
        description="보증금·선납금 0원, 월 납입료만 내세요"
        moreTo="/deals?type=nodeposit"
      />
      <InstantSection />

      {/* 모바일·태블릿: 이용 절차는 맨 아래 (PC 는 배너 아래에 표시) */}
      <UsageSteps className="lg:hidden" />
    </div>
  )
}

function HomeBanners() {
  const { data } = useBanners()
  if (!data || data.length === 0) {
    return <div className="aspect-[16/9] rounded-2xl bg-gradient-to-br from-primary-800 to-primary-500 md:aspect-[3/1]" />
  }
  return <BannerSlider banners={data} />
}

function DealSection({
  type,
  title,
  description,
  moreTo,
}: {
  type: DealType
  title: string
  description: string
  moreTo: string
}) {
  const { data, isPending, isError, refetch } = useDeals(type)

  return (
    <section aria-label={title}>
      <SectionHeader title={title} description={description} moreTo={moreTo} />
      {isPending ? (
        <CardSkeletonGrid />
      ) : isError ? (
        <ErrorState onRetry={() => refetch()} />
      ) : data.length === 0 ? (
        <EmptyState message="현재 진행 중인 특가가 없습니다." />
      ) : (
        <CardRow>
          {data.slice(0, HOME_CARD_LIMIT).map((deal) => (
            <CardRowItem key={deal.id}>
              <DealCard deal={deal} className="h-full" />
            </CardRowItem>
          ))}
        </CardRow>
      )}
    </section>
  )
}

function InstantSection() {
  const { data, isPending, isError, refetch } = useInstantStocks()

  return (
    <section aria-label="즉시출고">
      <SectionHeader
        title="즉시출고"
        description={data ? `기다림 없이 바로 받는 차량 · 총 ${data.totalCount}대 대기중` : '기다림 없이 바로 받는 차량'}
        moreTo="/instant"
      />
      {isPending ? (
        <CardSkeletonGrid />
      ) : isError ? (
        <ErrorState onRetry={() => refetch()} />
      ) : data.items.length === 0 ? (
        <EmptyState message="현재 즉시출고 가능한 차량이 없습니다." />
      ) : (
        <CardRow>
          {data.items.slice(0, HOME_CARD_LIMIT).map((stock) => (
            <CardRowItem key={stock.id}>
              <InstantStockCard stock={stock} className="h-full" />
            </CardRowItem>
          ))}
        </CardRow>
      )}
    </section>
  )
}
