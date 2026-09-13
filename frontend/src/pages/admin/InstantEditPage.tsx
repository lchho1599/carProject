import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { AdminLoadError, AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { SelectField, TextField, ToggleField } from '../../components/admin/form'
import { fieldErrorMap, formErrorMessage, toNumberOrNull } from '../../components/admin/formUtils'
import { VehicleTrimPicker } from '../../components/admin/VehicleTrimPicker'
import { Button } from '../../components/ui/Button'
import { useAdminInstant, useDeleteResource, useSaveResource, type AdminInstant, type InstantInput } from '../../features/admin/catalog'
import { stockStatusLabel } from '../../features/admin/labels'
import type { StockStatus } from '../../features/instant/api'

/** 즉시출고 재고 등록(/admin/instant/new) · 수정(/admin/instant/:stockId) */
export function InstantEditPage() {
  const { stockId: idParam } = useParams()
  const isNew = idParam === 'new'
  const stockId = isNew ? undefined : Number(idParam)
  const stock = useAdminInstant(stockId)

  if (!isNew && stock.isPending) return <AdminLoading />
  if (!isNew && (stock.isError || !stock.data)) {
    return <AdminLoadError error={stock.error} onRetry={() => stock.refetch()} backTo="/admin/instant" />
  }
  return <InstantForm key={stock.data?.id ?? 'new'} stock={stock.data} />
}

function InstantForm({ stock }: { stock?: AdminInstant }) {
  const navigate = useNavigate()
  const [trimId, setTrimId] = useState<number | null>(stock?.vehicle.trimId ?? null)
  const [form, setForm] = useState({
    exteriorColor: stock?.exteriorColor ?? '',
    interiorColor: stock?.interiorColor ?? '',
    optionsText: stock?.optionsText ?? '',
    vehiclePrice: stock ? String(stock.vehiclePrice) : '',
    monthlyPrice: stock ? String(stock.monthlyPrice) : '',
    conditionText: stock?.conditionText ?? '48개월 / 선납금 30% 기준',
    badge: stock?.badge ?? '5일이내 출고',
    status: stock?.status ?? ('AVAILABLE' as StockStatus),
    sortOrder: String(stock?.sortOrder ?? 0),
    published: stock?.published ?? false,
  })
  const save = useSaveResource<InstantInput, AdminInstant>('instant')
  const remove = useDeleteResource('instant')
  const errors = fieldErrorMap(save.error)
  const message = formErrorMessage(save.error)
  const set = <K extends keyof typeof form>(key: K, value: (typeof form)[K]) => setForm((current) => ({ ...current, [key]: value }))

  const submit = () => {
    const input: InstantInput = {
      trimId,
      exteriorColor: form.exteriorColor,
      interiorColor: form.interiorColor || null,
      optionsText: form.optionsText || null,
      vehiclePrice: toNumberOrNull(form.vehiclePrice) as number,
      monthlyPrice: toNumberOrNull(form.monthlyPrice) as number,
      conditionText: form.conditionText,
      badge: form.badge || null,
      status: form.status,
      sortOrder: toNumberOrNull(form.sortOrder) ?? 0,
      published: form.published,
    }
    save.mutate({ id: stock?.id, input }, { onSuccess: () => navigate('/admin/instant') })
  }

  return (
    <div className="space-y-4">
      <AdminPageHeader title={stock ? '즉시출고 재고 수정' : '즉시출고 재고 등록'} backTo="/admin/instant" />
      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault()
          submit()
        }}
      >
        <section aria-label="차량" className="space-y-3 rounded-xl bg-white p-4 md:p-5">
          <h2 className="font-extrabold">차량</h2>
          <VehicleTrimPicker initialBrandId={stock?.vehicle.brandId} initialModelId={stock?.vehicle.modelId} trimId={trimId} onChange={setTrimId} error={errors.trimId} />
        </section>

        <section aria-label="재고 정보" className="grid gap-3 rounded-xl bg-white p-4 sm:grid-cols-2 md:p-5 lg:grid-cols-3">
          <h2 className="font-extrabold sm:col-span-2 lg:col-span-3">재고 정보</h2>
          <TextField label="외장색" required value={form.exteriorColor} error={errors.exteriorColor} onChange={(e) => set('exteriorColor', e.target.value)} />
          <TextField label="내장색" value={form.interiorColor} error={errors.interiorColor} onChange={(e) => set('interiorColor', e.target.value)} />
          <TextField label="추가 옵션" value={form.optionsText} error={errors.optionsText} placeholder="예: 파노라마 선루프, 빌트인 캠" onChange={(e) => set('optionsText', e.target.value)} />
          <TextField label="차량가(원)" required inputMode="numeric" value={form.vehiclePrice} error={errors.vehiclePrice} onChange={(e) => set('vehiclePrice', e.target.value)} />
          <TextField label="월 납입료(원)" required inputMode="numeric" value={form.monthlyPrice} error={errors.monthlyPrice} onChange={(e) => set('monthlyPrice', e.target.value)} />
          <TextField label="조건 문구" required value={form.conditionText} error={errors.conditionText} onChange={(e) => set('conditionText', e.target.value)} />
          <TextField label="배지" value={form.badge} error={errors.badge} onChange={(e) => set('badge', e.target.value)} />
          <SelectField label="재고 상태" required value={form.status} error={errors.status} hint="판매완료는 사용자 화면에서 빠집니다." onChange={(e) => set('status', e.target.value as StockStatus)}>
            {(Object.keys(stockStatusLabel) as StockStatus[]).map((value) => (
              <option key={value} value={value}>
                {stockStatusLabel[value]}
              </option>
            ))}
          </SelectField>
          <TextField label="정렬 순서" type="number" min={0} value={form.sortOrder} onChange={(e) => set('sortOrder', e.target.value)} />
          <div className="sm:col-span-2 lg:col-span-3">
            <ToggleField label="공개" description="끄면 사용자 화면에 보이지 않습니다." checked={form.published} onChange={(published) => set('published', published)} />
          </div>
        </section>

        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>}
        <div className="flex flex-wrap justify-between gap-2">
          <Button type="submit" loading={save.isPending}>
            {stock ? '수정 저장' : '재고 등록'}
          </Button>
          {stock && (
            <Button
              variant="outline"
              className="text-red-600"
              loading={remove.isPending}
              onClick={() => {
                if (window.confirm('이 재고를 삭제할까요? 판매가 끝난 차량은 삭제 대신 "판매완료"로 바꾸는 것을 권장합니다.')) {
                  remove.mutate(stock.id, { onSuccess: () => navigate('/admin/instant') })
                }
              }}
            >
              삭제
            </Button>
          )}
        </div>
      </form>
    </div>
  )
}
