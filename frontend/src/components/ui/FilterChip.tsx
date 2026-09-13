import { cn } from '../../lib/cn'

type Props = {
  selected: boolean
  onClick: () => void
  children: React.ReactNode
}

/** 목록 필터·탭용 선택 칩 */
export function FilterChip({ selected, onClick, children }: Props) {
  return (
    <button
      type="button"
      aria-pressed={selected}
      onClick={onClick}
      className={cn(
        'shrink-0 rounded-full border px-4 py-2 text-sm font-semibold whitespace-nowrap transition-colors',
        selected ? 'border-primary bg-primary text-white' : 'border-gray-300 bg-white text-gray-700 hover:border-primary-300',
      )}
    >
      {children}
    </button>
  )
}
