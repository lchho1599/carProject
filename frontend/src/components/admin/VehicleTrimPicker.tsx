import { useState } from 'react'
import { useAdminBrands, useAdminModel, useAdminModels } from '../../features/admin/catalog'
import { formatManwon } from '../../lib/format'
import { SelectField } from './form'

type Props = {
  /** 수정 화면이면 기존 차량의 브랜드·모델 (선택 목록을 미리 채우기 위해) */
  initialBrandId?: number
  initialModelId?: number
  trimId: number | null
  onChange: (trimId: number | null) => void
  error?: string
}

/** 특가·즉시출고 등록용 차량 선택: 브랜드 → 모델 → 세부모델(트림). 사용 안 함인 항목은 (사용 안 함) 표시 */
export function VehicleTrimPicker({ initialBrandId, initialModelId, trimId, onChange, error }: Props) {
  const [brandId, setBrandId] = useState(initialBrandId)
  const [modelId, setModelId] = useState(initialModelId)
  const brands = useAdminBrands()
  const models = useAdminModels(brandId)
  const model = useAdminModel(modelId)

  return (
    <div className="grid gap-3 sm:grid-cols-3">
      <SelectField
        label="브랜드"
        required
        value={brandId ?? ''}
        onChange={(event) => {
          setBrandId(event.target.value ? Number(event.target.value) : undefined)
          setModelId(undefined)
          onChange(null)
        }}
      >
        <option value="">선택</option>
        {brands.data?.map((brand) => (
          <option key={brand.id} value={brand.id}>
            {brand.name}
            {!brand.active && ' (사용 안 함)'}
          </option>
        ))}
      </SelectField>
      <SelectField
        label="모델"
        required
        disabled={!brandId}
        value={modelId ?? ''}
        onChange={(event) => {
          setModelId(event.target.value ? Number(event.target.value) : undefined)
          onChange(null)
        }}
      >
        <option value="">선택</option>
        {models.data?.map((item) => (
          <option key={item.id} value={item.id}>
            {item.name}
            {!item.active && ' (사용 안 함)'}
          </option>
        ))}
      </SelectField>
      <SelectField
        label="세부모델"
        required
        disabled={!modelId}
        error={error}
        value={trimId ?? ''}
        onChange={(event) => onChange(event.target.value ? Number(event.target.value) : null)}
      >
        <option value="">선택</option>
        {model.data?.trims.map((trim) => (
          <option key={trim.id} value={trim.id}>
            {trim.name} ({formatManwon(trim.price)}){!trim.active && ' (사용 안 함)'}
          </option>
        ))}
      </SelectField>
    </div>
  )
}
