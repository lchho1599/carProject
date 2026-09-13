import { useState, type ReactNode } from 'react'
import { Link, useParams, useSearchParams } from 'react-router'
import { ChoiceGroup } from '../../components/estimate/ChoiceGroup'
import { LeadForm } from '../../components/lead/LeadForm'
import { ErrorState } from '../../components/ui/StateViews'
import { Spinner } from '../../components/ui/Spinner'
import { VehicleImage } from '../../components/vehicle/VehicleImage'
import {
  CREDIT_SCORES,
  DEFAULT_CONDITIONS,
  INSURANCE_AGES,
  MILEAGES,
  PERIODS,
  RATES,
  USE_TYPES,
} from '../../features/estimate/conditions'
import { calculateEstimatePrice, initialTrimId } from '../../features/estimate/price'
import type { LeadConditions } from '../../features/lead/types'
import { useModel } from '../../features/vehicle/api'
import { bodyTypeLabel, fuelLabel } from '../../features/vehicle/labels'
import type { ModelDetail } from '../../features/vehicle/types'
import { ApiError } from '../../lib/apiClient'
import { cn } from '../../lib/cn'
import { formatManwon, formatWon } from '../../lib/format'
import { StepHeader } from './EstimatePage'

const APPLY_SECTION_ID = 'estimate-apply'

/** 간편견적 2단계 — 트림·색상·옵션·이용조건 선택 후 견적 상담 신청 */
export function EstimateDetailPage() {
  const modelId = Number(useParams().modelId)
  const [searchParams] = useSearchParams()
  const { data: model, isPending, error, refetch } = useModel(Number.isFinite(modelId) ? modelId : undefined)

  if (!Number.isFinite(modelId) || (error instanceof ApiError && error.status === 404)) {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <h1 className="text-xl font-extrabold">견적 가능한 차량을 찾을 수 없습니다</h1>
        <p className="mt-2 text-gray-500">판매가 종료되었거나 주소가 잘못되었습니다.</p>
        <Link to="/estimate" className="mt-6 inline-flex h-11 items-center rounded-lg bg-primary px-5 font-bold text-white">
          차량 다시 선택하기
        </Link>
      </div>
    )
  }
  if (isPending) {
    return (
      <div className="flex justify-center py-24 text-primary">
        <Spinner className="size-8" />
      </div>
    )
  }
  if (error || !model) {
    return (
      <div className="mx-auto max-w-md px-4 py-16">
        <ErrorState onRetry={() => refetch()} />
      </div>
    )
  }

  // 다른 모델로 이동하면 선택 상태를 새로 시작하도록 key 로 구분
  return <EstimateBuilder key={model.id} model={model} trimIdParam={searchParams.get('trimId')} />
}

function EstimateBuilder({ model, trimIdParam }: { model: ModelDetail; trimIdParam: string | null }) {
  const [trimId, setTrimId] = useState(() => initialTrimId(model, trimIdParam))
  const [colorId, setColorId] = useState<number>()
  const [optionIds, setOptionIds] = useState<number[]>([])
  const [conditions, setConditions] = useState<Required<LeadConditions>>(DEFAULT_CONDITIONS)

  const vehicleName = `${model.brand.name} ${model.name}`
  const price = calculateEstimatePrice(model, { trimId, colorId, optionIds })
  const selectedTrim = model.trims.find((trim) => trim.id === trimId)
  const selectedColor = model.colors.find((color) => color.id === colorId)
  const setCondition = <K extends keyof LeadConditions>(key: K, value: Required<LeadConditions>[K]) =>
    setConditions((current) => ({ ...current, [key]: value }))
  const toggleOption = (id: number) =>
    setOptionIds((current) => (current.includes(id) ? current.filter((value) => value !== id) : [...current, id]))

  return (
    <div className="mx-auto max-w-6xl px-4 py-6 pb-28 md:py-10 md:pb-10">
      <StepHeader step={2} title="옵션·조건 선택" description="원하는 사양과 이용조건을 고르면 조건에 맞는 최저가 견적을 안내해 드립니다." />

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_380px] lg:items-start">
        <div className="space-y-5">
          <section className="grid gap-4 rounded-2xl bg-white p-4 sm:grid-cols-[220px_1fr] sm:items-center md:p-5">
            <VehicleImage src={model.imageUrl} alt={vehicleName} className="rounded-xl" />
            <div>
              <p className="text-sm font-semibold text-primary">{model.brand.name}</p>
              <h2 className="text-2xl font-extrabold">{model.name}</h2>
              <p className="mt-1 text-sm text-gray-500">
                {[model.segment, bodyTypeLabel[model.bodyType], fuelLabel[model.fuel]].filter(Boolean).join(' · ')}
              </p>
              <Link to={`/estimate?${model.brand.origin === 'IMPORTED' ? 'origin=IMPORTED&' : ''}brand=${model.brand.id}`} className="mt-3 inline-block text-sm font-semibold text-gray-500 underline underline-offset-2">
                다른 모델 선택
              </Link>
            </div>
          </section>

          <Section title="세부모델">
            <fieldset>
              <legend className="sr-only">세부모델</legend>
              <div className="space-y-2">
                {model.trims.map((trim) => (
                  <label
                    key={trim.id}
                    className="flex cursor-pointer items-center justify-between gap-3 rounded-lg border border-gray-200 px-4 py-3 has-checked:border-primary has-checked:bg-primary-50"
                  >
                    <span className="flex items-center gap-3">
                      <input
                        type="radio"
                        name="trim"
                        checked={trim.id === trimId}
                        onChange={() => setTrimId(trim.id)}
                        className="size-4 accent-primary"
                      />
                      <span className="text-sm font-semibold">{trim.name}</span>
                    </span>
                    <span className="shrink-0 text-sm font-bold">{formatManwon(trim.price)}</span>
                  </label>
                ))}
              </div>
            </fieldset>
          </Section>

          {model.colors.length > 0 && (
            <Section title="외장 색상" hint={selectedColor ? selectedColor.name : '선택하지 않으면 상담 시 정할 수 있어요'}>
              <fieldset>
                <legend className="sr-only">외장 색상</legend>
                <div className="flex flex-wrap gap-3">
                  <ColorOption label="미정" selected={colorId === undefined} onSelect={() => setColorId(undefined)} />
                  {model.colors.map((color) => (
                    <ColorOption
                      key={color.id}
                      label={color.name}
                      hexCode={color.hexCode}
                      extraPrice={color.extraPrice}
                      selected={color.id === colorId}
                      onSelect={() => setColorId(color.id)}
                    />
                  ))}
                </div>
              </fieldset>
            </Section>
          )}

          {model.options.length > 0 && (
            <Section title="옵션" hint="중복 선택 가능">
              <fieldset>
                <legend className="sr-only">옵션</legend>
                <div className="grid gap-2 sm:grid-cols-2">
                  {model.options.map((option) => (
                    <label
                      key={option.id}
                      className="flex cursor-pointer items-center justify-between gap-3 rounded-lg border border-gray-200 px-4 py-3 has-checked:border-primary has-checked:bg-primary-50"
                    >
                      <span className="flex items-center gap-3">
                        <input
                          type="checkbox"
                          checked={optionIds.includes(option.id)}
                          onChange={() => toggleOption(option.id)}
                          className="size-4 accent-primary"
                        />
                        <span className="text-sm font-semibold">{option.name}</span>
                      </span>
                      <span className="shrink-0 text-sm text-gray-600">+{formatManwon(option.price)}</span>
                    </label>
                  ))}
                </div>
              </fieldset>
            </Section>
          )}

          <Section title="이용조건">
            <div className="space-y-5">
              <ChoiceGroup legend="이용방법" choices={USE_TYPES} value={conditions.useType} onChange={(v) => setCondition('useType', v)} columns={2} />
              <ChoiceGroup legend="이용기간" choices={PERIODS} value={conditions.periodMonths} onChange={(v) => setCondition('periodMonths', v)} />
              <ChoiceGroup legend="보증금" choices={RATES} value={conditions.depositRate} onChange={(v) => setCondition('depositRate', v)} columns={5} />
              <ChoiceGroup legend="선납금" choices={RATES} value={conditions.prepayRate} onChange={(v) => setCondition('prepayRate', v)} columns={5} />
              <ChoiceGroup legend="보험연령" choices={INSURANCE_AGES} value={conditions.insuranceAge} onChange={(v) => setCondition('insuranceAge', v)} columns={2} />
              <ChoiceGroup legend="연간 주행거리" choices={MILEAGES} value={conditions.annualMileage} onChange={(v) => setCondition('annualMileage', v)} columns={4} />
              <ChoiceGroup legend="신용도" choices={CREDIT_SCORES} value={conditions.creditScore} onChange={(v) => setCondition('creditScore', v)} />
            </div>
          </Section>
        </div>

        <aside id={APPLY_SECTION_ID} className="scroll-mt-20 space-y-4 lg:sticky lg:top-24">
          <section aria-label="견적 요약" className="rounded-2xl bg-white p-5">
            <h2 className="font-extrabold">견적 요약</h2>
            <dl className="mt-3 space-y-2 text-sm">
              <SummaryRow label={selectedTrim ? selectedTrim.name : '세부모델'} value={formatWon(price.trimPrice)} />
              <SummaryRow label={`색상${selectedColor ? ` (${selectedColor.name})` : ''}`} value={price.colorPrice ? `+${formatWon(price.colorPrice)}` : '0원'} />
              <SummaryRow label={`옵션 ${optionIds.length}개`} value={price.optionPrice ? `+${formatWon(price.optionPrice)}` : '0원'} />
            </dl>
            <div className="mt-3 flex items-baseline justify-between border-t border-gray-100 pt-3">
              <span className="font-bold">합계 차량가</span>
              <strong className="text-2xl font-extrabold text-primary" aria-live="polite">
                {formatWon(price.total)}
              </strong>
            </div>
            <p className="mt-3 text-xs text-gray-500">
              월 납입료는 신용도·보험 조건 등에 따라 달라지므로 상담사가 조건별 최저가를 비교해 안내해 드립니다.
            </p>
          </section>

          <section aria-label="견적 신청" className="rounded-2xl bg-white p-5">
            <h2 className="mb-1 font-extrabold">최저가 견적 받기</h2>
            <p className="mb-4 text-sm text-gray-500">연락처를 남기면 선택하신 조건으로 견적을 안내해 드립니다.</p>
            <LeadForm
              context={{
                type: 'ESTIMATE',
                trimId,
                colorId,
                optionIds,
                conditions,
              }}
              submitLabel="견적 상담 신청"
            />
          </section>
        </aside>
      </div>

      {/* 모바일: 하단 메뉴 위에 합계와 신청 바로가기 */}
      <div className="fixed inset-x-0 bottom-bottom-bar z-30 flex items-center justify-between gap-3 border-t border-gray-200 bg-white px-4 py-2.5 shadow-[0_-4px_12px_rgba(0,0,0,0.06)] lg:hidden">
        <div>
          <p className="text-xs text-gray-500">{vehicleName} 합계</p>
          <p className="font-extrabold text-primary">{formatWon(price.total)}</p>
        </div>
        <a href={`#${APPLY_SECTION_ID}`} className="inline-flex h-10 items-center rounded-lg bg-accent px-4 text-sm font-bold text-white md:hidden">
          견적 신청
        </a>
        <a href={`#${APPLY_SECTION_ID}`} className="hidden h-10 items-center rounded-lg bg-accent px-4 text-sm font-bold text-white md:inline-flex">
          견적 신청하기
        </a>
      </div>
    </div>
  )
}

function Section({ title, hint, children }: { title: string; hint?: string; children: ReactNode }) {
  return (
    <section aria-label={title} className="rounded-2xl bg-white p-4 md:p-5">
      <div className="mb-3 flex items-baseline justify-between gap-2">
        <h2 className="text-lg font-extrabold">{title}</h2>
        {hint && <span className="text-sm text-gray-500">{hint}</span>}
      </div>
      {children}
    </section>
  )
}

function ColorOption({
  label,
  hexCode,
  extraPrice,
  selected,
  onSelect,
}: {
  label: string
  hexCode?: string | null
  extraPrice?: number
  selected: boolean
  onSelect: () => void
}) {
  return (
    <label className="flex w-20 cursor-pointer flex-col items-center gap-1 text-center">
      <input type="radio" name="color" checked={selected} onChange={onSelect} className="peer sr-only" />
      <span
        aria-hidden="true"
        className={cn(
          'size-11 rounded-full border-2 border-gray-200 peer-checked:border-primary peer-checked:ring-2 peer-checked:ring-primary-200 peer-focus-visible:outline-2 peer-focus-visible:outline-accent',
          !hexCode && 'flex items-center justify-center bg-gray-50 text-xs text-gray-500',
        )}
        style={hexCode ? { backgroundColor: hexCode } : undefined}
      >
        {!hexCode && '?'}
      </span>
      <span className="text-xs leading-tight font-semibold text-gray-700">{label}</span>
      {extraPrice ? <span className="text-[11px] text-accent-600">+{formatManwon(extraPrice)}</span> : null}
    </label>
  )
}

function SummaryRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-3">
      <dt className="min-w-0 truncate text-gray-600">{label}</dt>
      <dd className="shrink-0 font-semibold">{value}</dd>
    </div>
  )
}
