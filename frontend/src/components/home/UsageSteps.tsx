import { cn } from '../../lib/cn'
import { SectionHeader } from './SectionHeader'

const STEPS = [
  { title: '간편 상담 신청', description: '원하는 차량과 연락처만 남기면 끝' },
  { title: '맞춤 견적 안내', description: '조건별로 비교한 최저가 견적을 전화로 안내' },
  { title: '계약 · 차량 인도', description: '서류 안내부터 탁송까지 한 번에' },
]

type Props = {
  /** compact: PC 메인 배너 아래 빈 공간을 채우는 작은 형태 (남은 높이를 꽉 채움) */
  variant?: 'default' | 'compact'
  className?: string
}

/** 이용 절차 3단계 */
export function UsageSteps({ variant = 'default', className }: Props) {
  if (variant === 'compact') {
    return (
      <section aria-label="이용 절차" className={cn('flex flex-col rounded-2xl border border-gray-200 bg-white p-5', className)}>
        <div className="mb-3 flex items-baseline gap-2">
          <h2 className="text-lg font-extrabold">이용 절차</h2>
          <p className="text-sm text-gray-500">복잡한 과정 없이 3단계로 끝납니다</p>
        </div>
        <ol className="grid flex-1 grid-cols-3 gap-3">
          {STEPS.map((step, index) => (
            <li key={step.title} className="flex flex-col justify-center rounded-xl bg-gray-50 p-4">
              <span className="text-sm font-extrabold text-accent-600">STEP {index + 1}</span>
              <p className="mt-1 font-bold">{step.title}</p>
              <p className="mt-1 text-sm text-gray-500">{step.description}</p>
            </li>
          ))}
        </ol>
      </section>
    )
  }

  return (
    <section aria-label="이용 절차" className={className}>
      <SectionHeader title="이용 절차" description="복잡한 과정 없이 3단계로 끝납니다" />
      <ol className="grid gap-3 md:grid-cols-3">
        {STEPS.map((step, index) => (
          <li key={step.title} className="rounded-xl border border-gray-200 bg-white p-5">
            <span className="text-sm font-extrabold text-accent-600">STEP {index + 1}</span>
            <p className="mt-1 text-lg font-bold">{step.title}</p>
            <p className="mt-1 text-sm text-gray-500">{step.description}</p>
          </li>
        ))}
      </ol>
    </section>
  )
}
