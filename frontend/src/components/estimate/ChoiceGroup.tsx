import { useId } from 'react'
import { cn } from '../../lib/cn'

type Props<T extends string | number> = {
  legend: string
  choices: { value: T; label: string }[]
  value: T
  onChange: (value: T) => void
  /** 한 줄에 놓을 칸 수 (모바일 기준) */
  columns?: 2 | 3 | 4 | 5
}

const columnClass = { 2: 'grid-cols-2', 3: 'grid-cols-3', 4: 'grid-cols-4', 5: 'grid-cols-5' }

/** 라디오 버튼을 칸 모양으로 보여주는 단일 선택 그룹 (키보드 방향키로도 선택 가능) */
export function ChoiceGroup<T extends string | number>({ legend, choices, value, onChange, columns = 3 }: Props<T>) {
  const name = useId()

  return (
    <fieldset>
      <legend className="mb-2 text-sm font-bold text-gray-800">{legend}</legend>
      <div className={cn('grid gap-2', columnClass[columns])}>
        {choices.map((choice) => (
          <label
            key={String(choice.value)}
            className="flex h-11 cursor-pointer items-center justify-center rounded-lg border border-gray-300 bg-white px-1 text-center text-sm font-semibold text-gray-700 transition-colors has-checked:border-primary has-checked:bg-primary-50 has-checked:text-primary has-focus-visible:outline-2 has-focus-visible:outline-accent"
          >
            <input
              type="radio"
              name={name}
              value={String(choice.value)}
              checked={choice.value === value}
              onChange={() => onChange(choice.value)}
              className="sr-only"
            />
            {choice.label}
          </label>
        ))}
      </div>
    </fieldset>
  )
}
