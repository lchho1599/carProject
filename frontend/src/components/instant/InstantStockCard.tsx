import type { InstantStock } from '../../features/instant/api'
import { cn } from '../../lib/cn'
import { formatManwon, formatWon } from '../../lib/format'
import { useConsultModal } from '../lead/consultModalContext'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'
import { VehicleImage } from '../vehicle/VehicleImage'

type Props = { stock: InstantStock; className?: string }

export function InstantStockCard({ stock, className }: Props) {
  const { openConsult } = useConsultModal()
  const { vehicle } = stock
  const vehicleName = `${vehicle.brandName} ${vehicle.modelName}`
  const reserved = stock.status === 'RESERVED'

  return (
    <article className={cn('relative flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white', className)}>
      <div className="relative">
        <VehicleImage src={vehicle.imageUrl} alt={vehicleName} className={cn(reserved && 'opacity-60')} />
        <div className="absolute top-3 left-3 flex gap-1.5">
          {reserved ? <Badge tone="gray">예약중</Badge> : stock.badge && <Badge>{stock.badge}</Badge>}
        </div>
      </div>

      <div className="flex flex-1 flex-col p-4">
        <h3 className="font-bold">{vehicleName}</h3>
        <p className="mt-1 line-clamp-2 min-h-10 text-sm text-gray-500">{vehicle.trimName}</p>

        <dl className="mt-3 space-y-1 border-t border-gray-100 pt-3 text-sm">
          <Row label="외장/내장">
            {stock.exteriorColor}
            {stock.interiorColor && ` / ${stock.interiorColor}`}
          </Row>
          {stock.optionsText && <Row label="옵션">{stock.optionsText}</Row>}
          <Row label="차량가">{formatManwon(stock.vehiclePrice)}</Row>
        </dl>

        <p className="mt-3 flex items-baseline gap-1">
          <span className="text-sm text-gray-600">월</span>
          <strong className="text-2xl font-extrabold text-accent-600">{formatWon(stock.monthlyPrice)}</strong>
          <span className="text-sm text-gray-500">~</span>
        </p>
        <p className="text-xs text-gray-500">{stock.conditionText}</p>

        <Button
          variant="accent"
          fullWidth
          className="mt-4"
          onClick={() =>
            openConsult({
              type: 'INSTANT',
              instantStockId: stock.id,
              label: `즉시출고 · ${vehicleName} (${stock.exteriorColor})`,
            })
          }
        >
          {reserved ? '대기 상담신청' : '간편 상담신청'}
        </Button>
      </div>
    </article>
  )
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex gap-2">
      <dt className="w-16 shrink-0 text-gray-400">{label}</dt>
      <dd className="line-clamp-1 text-gray-700">{children}</dd>
    </div>
  )
}
