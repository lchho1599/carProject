import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { apiGet } from '../../lib/apiClient'
import type { BodyType, FuelType, Origin, VehicleSummary } from '../vehicle/types'

export type StockStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD'

export type InstantStock = {
  id: number
  exteriorColor: string
  interiorColor: string | null
  optionsText: string | null
  vehiclePrice: number
  monthlyPrice: number
  conditionText: string
  badge: string | null
  status: StockStatus
  vehicle: VehicleSummary
}

export type InstantStockList = { totalCount: number; items: InstantStock[] }

export type InstantFilter = {
  brandId?: number
  origin?: Origin
  bodyType?: BodyType
  fuel?: FuelType
}

export function instantQueryString(filter: InstantFilter): string {
  const params = new URLSearchParams()
  if (filter.brandId !== undefined) params.set('brandId', String(filter.brandId))
  if (filter.origin) params.set('origin', filter.origin)
  if (filter.bodyType) params.set('bodyType', filter.bodyType)
  if (filter.fuel) params.set('fuel', filter.fuel)
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function useInstantStocks(filter: InstantFilter = {}) {
  return useQuery({
    queryKey: ['instant', filter],
    queryFn: () => apiGet<InstantStockList>(`/api/instant${instantQueryString(filter)}`),
    // 필터를 바꾸는 동안 이전 목록을 유지해 화면이 깜빡이지 않게 한다
    placeholderData: keepPreviousData,
  })
}
