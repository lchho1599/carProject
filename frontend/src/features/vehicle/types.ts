// 백엔드 vehicle DTO 와 같은 형식

export type Origin = 'DOMESTIC' | 'IMPORTED'
export type BodyType = 'SEDAN' | 'SUV' | 'VAN' | 'TRUCK' | 'ETC'
export type FuelType = 'GASOLINE' | 'DIESEL' | 'HYBRID' | 'EV' | 'LPG'

export type Brand = {
  id: number
  name: string
  origin: Origin
  logoUrl: string | null
}

export type ModelSummary = {
  id: number
  name: string
  segment: string | null
  bodyType: BodyType
  fuel: FuelType
  imageUrl: string | null
  minPrice: number | null
}

export type ModelDetail = {
  id: number
  brand: Brand
  name: string
  segment: string | null
  bodyType: BodyType
  fuel: FuelType
  imageUrl: string | null
  trims: { id: number; name: string; price: number }[]
  options: { id: number; name: string; price: number }[]
  colors: { id: number; name: string; hexCode: string | null; extraPrice: number }[]
}

/** 특가·즉시출고 카드에 함께 오는 차량 요약 */
export type VehicleSummary = {
  brandId: number
  brandName: string
  modelId: number
  modelName: string
  bodyType: BodyType
  fuel: FuelType
  imageUrl: string | null
  trimId: number
  trimName: string
  trimPrice: number
}
