import { useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router'
import { AdminLoadError, AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { ActiveBadge } from '../../components/admin/DisplayStatusBadge'
import { SelectField, TextField, ToggleField } from '../../components/admin/form'
import { adminInputClass, fieldErrorMap, formErrorMessage } from '../../components/admin/formUtils'
import { ImageUploadField } from '../../components/admin/ImageUploadField'
import { Button } from '../../components/ui/Button'
import {
  useAdminBrands,
  useAdminModel,
  useSaveModel,
  useSaveModelItem,
  type AdminColor,
  type AdminModel,
  type ModelInput,
  type ModelItemKind,
} from '../../features/admin/catalog'
import { bodyTypeLabel, fuelLabel } from '../../features/vehicle/labels'
import type { BodyType, FuelType } from '../../features/vehicle/types'
import { ApiError } from '../../lib/apiClient'
import { cn } from '../../lib/cn'
import { formatWon } from '../../lib/format'

const BODY_TYPES = Object.keys(bodyTypeLabel) as BodyType[]
const FUELS = Object.keys(fuelLabel) as FuelType[]

/** 모델 등록(/admin/vehicles/models/new?brand=) · 수정(/admin/vehicles/models/:modelId) */
export function ModelEditPage() {
  const { modelId: modelIdParam } = useParams()
  const isNew = modelIdParam === 'new'
  const modelId = isNew ? undefined : Number(modelIdParam)
  const detail = useAdminModel(modelId)
  const brandParam = Number(useSearchParams()[0].get('brand'))

  if (!isNew && detail.isPending) return <AdminLoading />
  if (!isNew && (detail.isError || !detail.data)) {
    return <AdminLoadError error={detail.error} onRetry={() => detail.refetch()} backTo="/admin/vehicles" />
  }

  const model = detail.data?.model
  const backTo = `/admin/vehicles?brand=${model?.brandId ?? brandParam}`

  return (
    <div className="space-y-4">
      <AdminPageHeader title={model ? `${model.brandName} ${model.name}` : '모델 추가'} backTo={backTo} />
      <ModelInfoForm key={model?.id ?? 'new'} model={model} defaultBrandId={brandParam} />

      {detail.data && (
        <>
          <ItemTable
            kind="trims"
            modelId={detail.data.model.id}
            title="세부모델(트림)"
            hint="견적 화면의 세부모델과 가격입니다."
            items={detail.data.trims.map((trim) => ({ ...trim, hexCode: null, extraPrice: trim.price }))}
          />
          <ItemTable
            kind="options"
            modelId={detail.data.model.id}
            title="옵션"
            hint="견적 화면에서 중복 선택할 수 있는 옵션입니다."
            items={detail.data.options.map((option) => ({ ...option, hexCode: null, extraPrice: option.price }))}
          />
          <ItemTable kind="colors" modelId={detail.data.model.id} title="외장 색상" hint="추가금이 없으면 0으로 입력합니다." items={detail.data.colors} />
        </>
      )}
    </div>
  )
}

function ModelInfoForm({ model, defaultBrandId }: { model?: AdminModel; defaultBrandId: number }) {
  const navigate = useNavigate()
  const brands = useAdminBrands()
  const save = useSaveModel()
  const [input, setInput] = useState<ModelInput>(
    model ?? {
      brandId: defaultBrandId || 0,
      name: '',
      segment: '',
      bodyType: 'SUV',
      fuel: 'GASOLINE',
      imageUrl: null,
      sortOrder: 0,
      active: true,
    },
  )
  const [saved, setSaved] = useState(false)
  const errors = fieldErrorMap(save.error)
  const message = formErrorMessage(save.error)

  return (
    <form
      aria-label="기본 정보"
      className="grid gap-4 rounded-xl bg-white p-4 md:grid-cols-[260px_1fr] md:p-5"
      onSubmit={(event) => {
        event.preventDefault()
        setSaved(false)
        save.mutate(
          { id: model?.id, input },
          {
            onSuccess: (result) => {
              setSaved(true)
              if (!model) navigate(`/admin/vehicles/models/${result.id}`, { replace: true })
            },
          },
        )
      }}
    >
      <ImageUploadField label="대표 이미지" value={input.imageUrl} error={errors.imageUrl} hint="차량 옆모습 이미지를 권장합니다." onChange={(url) => setInput({ ...input, imageUrl: url })} />
      <div className="grid content-start gap-3 sm:grid-cols-2">
        <SelectField
          label="브랜드"
          required
          disabled={!!model}
          value={input.brandId || ''}
          error={errors.brandId}
          hint={model ? '브랜드는 바꿀 수 없습니다.' : undefined}
          onChange={(e) => setInput({ ...input, brandId: Number(e.target.value) })}
        >
          <option value="">선택</option>
          {brands.data?.map((brand) => (
            <option key={brand.id} value={brand.id}>
              {brand.name}
            </option>
          ))}
        </SelectField>
        <TextField label="모델명" required value={input.name} error={errors.name} onChange={(e) => setInput({ ...input, name: e.target.value })} />
        <TextField label="차급" placeholder="예: 중형" value={input.segment ?? ''} error={errors.segment} onChange={(e) => setInput({ ...input, segment: e.target.value })} />
        <SelectField label="차종" required value={input.bodyType} error={errors.bodyType} onChange={(e) => setInput({ ...input, bodyType: e.target.value as BodyType })}>
          {BODY_TYPES.map((type) => (
            <option key={type} value={type}>
              {bodyTypeLabel[type]}
            </option>
          ))}
        </SelectField>
        <SelectField label="연료" required value={input.fuel} error={errors.fuel} hint="전기를 고르면 즉시출고 '전기차' 필터에 나옵니다." onChange={(e) => setInput({ ...input, fuel: e.target.value as FuelType })}>
          {FUELS.map((fuel) => (
            <option key={fuel} value={fuel}>
              {fuelLabel[fuel]}
            </option>
          ))}
        </SelectField>
        <TextField label="정렬 순서" type="number" min={0} value={input.sortOrder} onChange={(e) => setInput({ ...input, sortOrder: Number(e.target.value) || 0 })} />
        <div className="sm:col-span-2">
          <ToggleField label="사용" description="끄면 견적·특가·즉시출고 화면에서 이 모델이 숨겨집니다." checked={input.active} onChange={(active) => setInput({ ...input, active })} />
        </div>
        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700 sm:col-span-2">{message}</p>}
        <div className="flex items-center gap-3 sm:col-span-2">
          <Button type="submit" loading={save.isPending}>
            {model ? '기본 정보 저장' : '모델 등록'}
          </Button>
          {saved && <span role="status" className="text-sm text-green-700">저장했습니다.</span>}
        </div>
      </div>
    </form>
  )
}

type ItemRow = { id: number; name: string; hexCode: string | null; extraPrice: number; sortOrder: number; active: boolean }

/** 트림·옵션·색상 표 — 줄마다 바로 수정, 맨 아래 줄에서 추가 */
function ItemTable({ kind, modelId, title, hint, items }: { kind: ModelItemKind; modelId: number; title: string; hint: string; items: ItemRow[] | AdminColor[] }) {
  const priceLabel = kind === 'colors' ? '추가금(원)' : '가격(원)'
  return (
    <section aria-label={title} className="rounded-xl bg-white p-4 md:p-5">
      <h2 className="text-lg font-extrabold">{title}</h2>
      <p className="mb-3 text-sm text-gray-500">{hint}</p>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[640px] text-sm">
          <thead className="text-left text-gray-500">
            <tr>
              <th className="py-2 pr-2 font-semibold">이름</th>
              {kind === 'colors' && <th className="w-32 px-2 font-semibold">색상 코드</th>}
              <th className="w-36 px-2 font-semibold">{priceLabel}</th>
              <th className="w-20 px-2 font-semibold">순서</th>
              <th className="w-24 px-2 font-semibold">사용</th>
              <th className="w-24 pl-2" />
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {items.map((item) => (
              <ItemEditorRow key={`${item.id}-${item.name}-${item.extraPrice}-${item.active}-${item.sortOrder}`} kind={kind} modelId={modelId} item={item} />
            ))}
            <ItemEditorRow key={`new-${items.length}`} kind={kind} modelId={modelId} />
          </tbody>
        </table>
      </div>
    </section>
  )
}

function ItemEditorRow({ kind, modelId, item }: { kind: ModelItemKind; modelId: number; item?: ItemRow }) {
  const save = useSaveModelItem(modelId, kind)
  const [name, setName] = useState(item?.name ?? '')
  const [hexCode, setHexCode] = useState(item?.hexCode ?? '')
  const [price, setPrice] = useState(item ? String(item.extraPrice) : '')
  const [sortOrder, setSortOrder] = useState(String(item?.sortOrder ?? 0))
  const [active, setActive] = useState(item?.active ?? true)

  const dirty =
    !item || name !== item.name || price !== String(item.extraPrice) || sortOrder !== String(item.sortOrder) ||
    active !== item.active || (kind === 'colors' && hexCode !== (item.hexCode ?? ''))
  const errors = fieldErrorMap(save.error)
  const errorText =
    Object.values(errors).join(' ') || (save.error instanceof ApiError ? save.error.message : save.error ? '저장하지 못했습니다.' : '')

  const submit = () => {
    const priceNumber = Number(price.replaceAll(',', ''))
    const input: Record<string, unknown> = {
      name,
      sortOrder: Number(sortOrder) || 0,
      active,
      ...(kind === 'colors' ? { hexCode: hexCode || null, extraPrice: priceNumber } : { price: priceNumber }),
    }
    save.mutate({ id: item?.id, input })
  }

  const label = item ? item.name : '새 항목'
  return (
    <>
      <tr className={cn(!item && 'bg-gray-50/60', item && !item.active && 'text-gray-400')}>
        <td className="py-2 pr-2">
          <input aria-label={`${label} 이름`} placeholder={item ? undefined : '새로 추가할 이름'} className={cn(adminInputClass, 'border-gray-300')} value={name} onChange={(e) => setName(e.target.value)} />
        </td>
        {kind === 'colors' && (
          <td className="px-2">
            <div className="flex items-center gap-1.5">
              <span aria-hidden="true" className="size-6 shrink-0 rounded-full border border-gray-300" style={{ backgroundColor: /^#[0-9A-Fa-f]{6}$/.test(hexCode) ? hexCode : 'transparent' }} />
              <input aria-label={`${label} 색상 코드`} placeholder="#FFFFFF" className={cn(adminInputClass, 'border-gray-300')} value={hexCode} onChange={(e) => setHexCode(e.target.value)} />
            </div>
          </td>
        )}
        <td className="px-2">
          <input aria-label={`${label} ${kind === 'colors' ? '추가금' : '가격'}`} inputMode="numeric" className={cn(adminInputClass, 'border-gray-300 text-right')} value={price} onChange={(e) => setPrice(e.target.value)} />
          {price && Number.isFinite(Number(price.replaceAll(',', ''))) && (
            <span className="mt-0.5 block text-right text-xs text-gray-400">{formatWon(Number(price.replaceAll(',', '')))}</span>
          )}
        </td>
        <td className="px-2">
          <input aria-label={`${label} 순서`} type="number" min={0} className={cn(adminInputClass, 'border-gray-300')} value={sortOrder} onChange={(e) => setSortOrder(e.target.value)} />
        </td>
        <td className="px-2">
          {item ? (
            <label className="flex items-center gap-2">
              <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} className="size-4 accent-primary" aria-label={`${label} 사용`} />
              <ActiveBadge active={active} />
            </label>
          ) : (
            <span className="text-xs text-gray-400">추가 시 사용</span>
          )}
        </td>
        <td className="pl-2 text-right">
          <Button size="sm" variant={item ? 'outline' : 'primary'} disabled={!dirty || !name.trim()} loading={save.isPending} onClick={submit}>
            {item ? '저장' : '추가'}
          </Button>
        </td>
      </tr>
      {errorText && (
        <tr>
          <td colSpan={6} className="pb-2 text-sm text-red-600" role="alert">
            {errorText}
          </td>
        </tr>
      )}
    </>
  )
}
