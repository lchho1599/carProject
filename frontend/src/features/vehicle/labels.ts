import type { BodyType, FuelType } from './types'

export const fuelLabel: Record<FuelType, string> = {
  GASOLINE: '가솔린',
  DIESEL: '디젤',
  HYBRID: '하이브리드',
  EV: '전기',
  LPG: 'LPG',
}

export const bodyTypeLabel: Record<BodyType, string> = {
  SEDAN: '세단',
  SUV: 'SUV',
  VAN: '승합',
  TRUCK: '트럭',
  ETC: '기타',
}
