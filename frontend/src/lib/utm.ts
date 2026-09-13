// 광고 유입 파라미터(utm_*) 보관 — 첫 방문 시 저장했다가 상담 신청할 때 함께 보낸다.
const STORAGE_KEY = 'rentdb.utm'

export type Utm = Record<string, string>

/** 현재 주소의 utm_* 파라미터를 세션에 저장한다. 이미 저장된 값이 있으면 덮어쓰지 않는다(첫 유입 기준). */
export function captureUtm(search: string = window.location.search): void {
  try {
    if (sessionStorage.getItem(STORAGE_KEY)) return
    const params = new URLSearchParams(search)
    const utm: Utm = {}
    params.forEach((value, key) => {
      if (key.startsWith('utm_') && value) utm[key] = value.slice(0, 200)
    })
    if (Object.keys(utm).length > 0) {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(utm))
    }
  } catch {
    // 저장소를 쓸 수 없는 환경(사생활 보호 모드 등)에서는 무시
  }
}

export function getUtm(): Utm | undefined {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as Utm) : undefined
  } catch {
    return undefined
  }
}
