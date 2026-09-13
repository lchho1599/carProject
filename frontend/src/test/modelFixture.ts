import type { ModelDetail } from '../features/vehicle/types'

export const sorentoDetail: ModelDetail = {
  id: 9,
  brand: { id: 2, name: '기아', origin: 'DOMESTIC', logoUrl: null },
  name: '쏘렌토',
  segment: '중형',
  bodyType: 'SUV',
  fuel: 'HYBRID',
  imageUrl: null,
  trims: [
    { id: 17, name: '하이브리드 프레스티지', price: 39900000 },
    { id: 18, name: '하이브리드 시그니처', price: 48600000 },
  ],
  options: [
    { id: 31, name: '파노라마 선루프', price: 1200000 },
    { id: 32, name: '빌트인 캠', price: 450000 },
  ],
  colors: [
    { id: 41, name: '스노우 화이트 펄', hexCode: '#F4F4F2', extraPrice: 80000 },
    { id: 42, name: '어비스 블랙 펄', hexCode: '#1B1C1E', extraPrice: 0 },
  ],
}
