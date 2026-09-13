import type { Deal } from '../features/deal/api'
import type { InstantStock } from '../features/instant/api'
import type { Brand, VehicleSummary } from '../features/vehicle/types'

export const sorento: VehicleSummary = {
  brandId: 2,
  brandName: '기아',
  modelId: 9,
  modelName: '쏘렌토',
  bodyType: 'SUV',
  fuel: 'HYBRID',
  imageUrl: null,
  trimId: 18,
  trimName: '2026년형 하이브리드 1.6T 프레스티지 2WD',
  trimPrice: 39900000,
}

export function makeDeal(overrides: Partial<Deal> = {}): Deal {
  return {
    id: 6,
    type: 'TIME_SALE',
    title: '기아 쏘렌토 하이브리드',
    badge: 'HOT SALE',
    originalMonthly: 230000,
    monthlyPrice: 209000,
    leaseMonthly: 188000,
    periodMonths: 48,
    depositRate: 0,
    prepayRate: 30,
    endsAt: null,
    vehicle: sorento,
    ...overrides,
  }
}

export function makeStock(overrides: Partial<InstantStock> = {}): InstantStock {
  return {
    id: 3,
    exteriorColor: '스노우 화이트 펄',
    interiorColor: '블랙',
    optionsText: '기본가',
    vehiclePrice: 27200000,
    monthlyPrice: 213000,
    conditionText: '48개월 / 선납금 30% 기준',
    badge: '5일이내 출고',
    status: 'AVAILABLE',
    vehicle: { ...sorento, modelId: 7, modelName: 'K5', bodyType: 'SEDAN', fuel: 'GASOLINE', trimId: 13 },
    ...overrides,
  }
}

export const brands: Brand[] = [
  { id: 1, name: '현대', origin: 'DOMESTIC', logoUrl: null },
  { id: 2, name: '기아', origin: 'DOMESTIC', logoUrl: null },
  { id: 5, name: 'BMW', origin: 'IMPORTED', logoUrl: null },
]
