import { useQuery } from '@tanstack/react-query'
import { apiGet } from '../../lib/apiClient'
import type { VehicleSummary } from '../vehicle/types'

export type DealType = 'TIME_SALE' | 'NO_DEPOSIT'

export type Deal = {
  id: number
  type: DealType
  title: string
  badge: string | null
  originalMonthly: number | null
  monthlyPrice: number
  leaseMonthly: number | null
  periodMonths: number
  depositRate: number
  prepayRate: number
  /** ISO-8601 (+09:00), 상시 특가면 null */
  endsAt: string | null
  vehicle: VehicleSummary
}

/** 주소창 파라미터(time / nodeposit) ↔ API 유형 */
export const dealTypeByParam: Record<string, DealType> = {
  time: 'TIME_SALE',
  nodeposit: 'NO_DEPOSIT',
}

export const dealTypeLabel: Record<DealType, string> = {
  TIME_SALE: '타임특가',
  NO_DEPOSIT: '무보증특가',
}

export function useDeals(type: DealType) {
  return useQuery({
    queryKey: ['deals', type],
    queryFn: () => apiGet<Deal[]>(`/api/deals?type=${type}`),
  })
}
