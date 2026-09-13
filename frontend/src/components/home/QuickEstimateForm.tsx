import { useState } from 'react'
import { useBrands, useModels } from '../../features/vehicle/api'
import type { LeadContext } from '../../features/lead/types'
import { LeadForm } from '../lead/LeadForm'

const PERIODS = [36, 48, 60] as const

const selectClass =
  'h-12 w-full rounded-lg border border-gray-300 bg-white px-3 text-base focus:border-primary focus:outline-none disabled:bg-gray-50 disabled:text-gray-400'

/** 메인 빠른견적: 브랜드 → 모델 → 계약기간 선택 후 연락처 입력 (선택 항목은 모두 생략 가능) */
export function QuickEstimateForm() {
  const [brandId, setBrandId] = useState<number>()
  const [modelId, setModelId] = useState<number>()
  const [periodMonths, setPeriodMonths] = useState<number>(48)

  const brands = useBrands()
  const models = useModels(brandId)

  const context: LeadContext = {
    type: 'QUICK',
    brandId,
    modelId,
    conditions: { periodMonths },
  }

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-2">
        <label className="sr-only" htmlFor="quick-brand">
          브랜드
        </label>
        <select
          id="quick-brand"
          className={selectClass}
          value={brandId ?? ''}
          onChange={(event) => {
            setBrandId(event.target.value ? Number(event.target.value) : undefined)
            setModelId(undefined)
          }}
        >
          <option value="">브랜드 선택</option>
          {brands.data?.map((brand) => (
            <option key={brand.id} value={brand.id}>
              {brand.name}
            </option>
          ))}
        </select>

        <label className="sr-only" htmlFor="quick-model">
          모델
        </label>
        <select
          id="quick-model"
          className={selectClass}
          value={modelId ?? ''}
          disabled={!brandId || models.isLoading}
          onChange={(event) => setModelId(event.target.value ? Number(event.target.value) : undefined)}
        >
          <option value="">{brandId ? '모델 선택' : '브랜드 먼저 선택'}</option>
          {models.data?.map((model) => (
            <option key={model.id} value={model.id}>
              {model.name}
            </option>
          ))}
        </select>
      </div>

      <fieldset>
        <legend className="mb-1 text-sm font-semibold text-gray-700">계약기간</legend>
        <div className="grid grid-cols-3 gap-2">
          {PERIODS.map((period) => (
            <label
              key={period}
              className="flex h-11 cursor-pointer items-center justify-center rounded-lg border border-gray-300 text-sm font-semibold has-checked:border-primary has-checked:bg-primary-50 has-checked:text-primary"
            >
              <input
                type="radio"
                name="quick-period"
                value={period}
                checked={periodMonths === period}
                onChange={() => setPeriodMonths(period)}
                className="sr-only"
              />
              {period}개월
            </label>
          ))}
        </div>
      </fieldset>

      <LeadForm context={context} submitLabel="무료 견적 받기" />
    </div>
  )
}
