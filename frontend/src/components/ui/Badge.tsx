import type { ReactNode } from 'react'
import { cn } from '../../lib/cn'

type Tone = 'accent' | 'primary' | 'gray'

const toneClass: Record<Tone, string> = {
  accent: 'bg-accent-50 text-accent-700',
  primary: 'bg-primary-50 text-primary-700',
  gray: 'bg-gray-100 text-gray-600',
}

export function Badge({ tone = 'accent', children, className }: { tone?: Tone; children: ReactNode; className?: string }) {
  return (
    <span className={cn('inline-flex items-center rounded px-2 py-0.5 text-xs font-bold', toneClass[tone], className)}>
      {children}
    </span>
  )
}
