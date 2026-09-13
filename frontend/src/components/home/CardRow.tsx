import type { ReactNode } from 'react'

/**
 * 메인 섹션 카드 줄 — 모바일은 가로 스크롤, PC는 4열 그리드.
 * relative: 카드 안의 absolute 요소(화면낭독기용 sr-only 글자 등)가 스크롤 영역 밖으로 빠져나가
 * 모바일 페이지 전체 폭을 늘리지 않도록 이 영역을 기준 위치로 삼는다.
 */
export function CardRow({ children }: { children: ReactNode }) {
  return (
    <div className="relative -mx-4 flex snap-x snap-mandatory gap-3 overflow-x-auto px-4 pb-2 md:mx-0 md:grid md:grid-cols-2 md:gap-4 md:overflow-visible md:px-0 lg:grid-cols-4">
      {children}
    </div>
  )
}

/** CardRow 안의 카드 한 칸 (모바일에서 화면의 약 80% 폭) */
export function CardRowItem({ children }: { children: ReactNode }) {
  return <div className="w-[80%] shrink-0 snap-start sm:w-[45%] md:w-auto">{children}</div>
}
