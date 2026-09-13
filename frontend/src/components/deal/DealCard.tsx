import { Link } from 'react-router'
import type { Deal } from '../../features/deal/api'
import { cn } from '../../lib/cn'
import { dDayLabel } from '../../lib/date'
import { formatWon } from '../../lib/format'
import { useConsultModal } from '../lead/consultModalContext'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { VehicleImage } from '../vehicle/VehicleImage'

type Props = { deal: Deal; className?: string }

/**
 * 특가 카드
 * - 타임특가: "견적 확인" → 간편견적 2단계(트림 선택된 상태), "바로 상담" → 특가 상담 모달
 * - 무보증특가: "간편 상담신청" → 특가 상담 모달
 */
export function DealCard({ deal, className }: Props) {
  const { openConsult } = useConsultModal()
  const { vehicle } = deal
  const vehicleName = `${vehicle.brandName} ${vehicle.modelName}`
  const dDay = dDayLabel(deal.endsAt)
  const isNoDeposit = deal.type === 'NO_DEPOSIT'
  const discountRate =
    deal.originalMonthly && deal.originalMonthly > deal.monthlyPrice
      ? Math.round((1 - deal.monthlyPrice / deal.originalMonthly) * 100)
      : null

  const openDealConsult = () =>
    openConsult({
      type: 'DEAL',
      dealId: deal.id,
      label: `${deal.title} · ${formatWon(deal.monthlyPrice)}/월`,
    })

  return (
    <article className={cn('relative flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white', className)}>
      <div className="relative">
        <VehicleImage src={vehicle.imageUrl} alt={vehicleName} />
        <div className="absolute top-3 left-3 flex gap-1.5">
          {deal.badge && <Badge>{deal.badge}</Badge>}
          {dDay && <Badge tone="primary">{dDay}</Badge>}
        </div>
      </div>

      <div className="flex flex-1 flex-col p-4">
        <h3 className="font-bold">{vehicleName}</h3>
        <p className="mt-1 line-clamp-2 min-h-10 text-sm text-gray-500">{vehicle.trimName}</p>

        <div className="mt-3 border-t border-gray-100 pt-3">
          {deal.originalMonthly !== null && (
            <p className="text-sm text-gray-400">
              <span className="sr-only">정가 </span>
              <del>{formatWon(deal.originalMonthly)}</del>
            </p>
          )}
          <p className="flex items-baseline gap-1.5">
            {discountRate !== null && <span className="text-lg font-extrabold text-accent-600">{discountRate}%</span>}
            <span className="text-sm text-gray-600">월</span>
            <strong className="text-2xl font-extrabold text-gray-900">{formatWon(deal.monthlyPrice)}</strong>
          </p>
          {deal.leaseMonthly !== null && (
            <p className="mt-0.5 text-sm text-gray-500">리스 월 {formatWon(deal.leaseMonthly)}</p>
          )}
          <p className="mt-2 text-xs text-gray-500">
            {deal.periodMonths}개월 ·{' '}
            {isNoDeposit ? '보증금·선납금 0원' : `보증금 ${deal.depositRate}% · 선납금 ${deal.prepayRate}%`}
          </p>
        </div>

        <div className="mt-4 flex gap-2">
          {isNoDeposit ? (
            <Button variant="accent" fullWidth onClick={openDealConsult}>
              간편 상담신청
            </Button>
          ) : (
            <>
              <Link
                to={`/estimate/${vehicle.modelId}?trimId=${vehicle.trimId}`}
                className="inline-flex h-11 flex-1 items-center justify-center rounded-lg bg-primary font-semibold text-white hover:bg-primary-800"
              >
                견적 확인
              </Link>
              <Button variant="outline" className="flex-1" onClick={openDealConsult}>
                바로 상담
              </Button>
            </>
          )}
        </div>
      </div>
    </article>
  )
}
