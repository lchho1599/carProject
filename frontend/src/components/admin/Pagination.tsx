import { cn } from '../../lib/cn'

type Props = {
  /** 0부터 시작 */
  page: number
  totalPages: number
  onChange: (page: number) => void
}

/** 현재 페이지 앞뒤 2개씩 번호를 보여주는 간단한 페이지 이동 */
export function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null
  const start = Math.max(0, page - 2)
  const end = Math.min(totalPages - 1, page + 2)
  const pages = Array.from({ length: end - start + 1 }, (_, index) => start + index)

  return (
    <nav aria-label="페이지 이동" className="flex items-center justify-center gap-1">
      <PageButton disabled={page === 0} onClick={() => onChange(page - 1)} label="이전 페이지">
        ‹
      </PageButton>
      {pages.map((number) => (
        <PageButton
          key={number}
          current={number === page}
          onClick={() => onChange(number)}
          label={`${number + 1}페이지`}
        >
          {number + 1}
        </PageButton>
      ))}
      <PageButton disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)} label="다음 페이지">
        ›
      </PageButton>
    </nav>
  )
}

function PageButton({
  children,
  onClick,
  disabled,
  current,
  label,
}: {
  children: React.ReactNode
  onClick: () => void
  disabled?: boolean
  current?: boolean
  label: string
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      aria-label={label}
      aria-current={current ? 'page' : undefined}
      className={cn(
        'min-w-9 rounded-md px-2 py-1.5 text-sm font-semibold disabled:opacity-40',
        current ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100',
      )}
    >
      {children}
    </button>
  )
}
