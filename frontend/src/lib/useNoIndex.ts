import { useEffect } from 'react'

/** 관리자 화면(로그인 포함)은 검색엔진에 노출하지 않는다 */
export function useNoIndex(): void {
  useEffect(() => {
    const meta = document.createElement('meta')
    meta.name = 'robots'
    meta.content = 'noindex, nofollow'
    document.head.appendChild(meta)
    return () => meta.remove()
  }, [])
}
