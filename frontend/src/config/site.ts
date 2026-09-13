// 사이트 공통 정보 — 서버(/api/site) 응답 전에 쓰는 기본값. 정식 사이트명이 정해지면 .env의 VITE_SITE_NAME만 바꾼다.
export const site = {
  name: import.meta.env.VITE_SITE_NAME || '렌트DB(가칭)',
} as const

/** 상단·하단 메뉴 */
export const mainMenu = [
  { to: '/estimate', label: '간편견적' },
  { to: '/deals?type=time', label: '타임특가', match: '/deals' },
  { to: '/instant', label: '즉시출고' },
] as const
