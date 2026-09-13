import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { AdminLoadError, AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { DisplayStatusBadge } from '../../components/admin/DisplayStatusBadge'
import { SelectField, TextField, ToggleField } from '../../components/admin/form'
import { fieldErrorMap, formErrorMessage, toNumberOrNull } from '../../components/admin/formUtils'
import { VehicleTrimPicker } from '../../components/admin/VehicleTrimPicker'
import { Button } from '../../components/ui/Button'
import { useAdminDeal, useDeleteResource, useSaveResource, type AdminDeal, type DealInput } from '../../features/admin/catalog'
import { dealTypeLabel, type DealType } from '../../features/deal/api'
import { fromDatetimeLocal, nowDatetimeLocal, toDatetimeLocal } from '../../lib/datetime'

/** 특가 등록(/admin/deals/new) · 수정(/admin/deals/:dealId) */
export function DealEditPage() {
  const { dealId: idParam } = useParams()
  const isNew = idParam === 'new'
  const dealId = isNew ? undefined : Number(idParam)
  const deal = useAdminDeal(dealId)

  if (!isNew && deal.isPending) return <AdminLoading />
  if (!isNew && (deal.isError || !deal.data)) {
    return <AdminLoadError error={deal.error} onRetry={() => deal.refetch()} backTo="/admin/deals" />
  }
  return <DealForm key={deal.data?.id ?? 'new'} deal={deal.data} />
}

type FormState = {
  type: DealType
  trimId: number | null
  title: string
  badge: string
  originalMonthly: string
  monthlyPrice: string
  leaseMonthly: string
  periodMonths: number
  depositRate: string
  prepayRate: string
  startsAt: string
  endsAt: string
  sortOrder: string
  published: boolean
}

function initialState(deal?: AdminDeal): FormState {
  if (!deal) {
    return {
      type: 'TIME_SALE', trimId: null, title: '', badge: '', originalMonthly: '', monthlyPrice: '', leaseMonthly: '',
      periodMonths: 48, depositRate: '0', prepayRate: '30', startsAt: nowDatetimeLocal(), endsAt: '', sortOrder: '0', published: false,
    }
  }
  return {
    type: deal.type,
    trimId: deal.vehicle.trimId,
    title: deal.title,
    badge: deal.badge ?? '',
    originalMonthly: deal.originalMonthly?.toString() ?? '',
    monthlyPrice: String(deal.monthlyPrice),
    leaseMonthly: deal.leaseMonthly?.toString() ?? '',
    periodMonths: deal.periodMonths,
    depositRate: String(deal.depositRate),
    prepayRate: String(deal.prepayRate),
    startsAt: toDatetimeLocal(deal.startsAt),
    endsAt: toDatetimeLocal(deal.endsAt),
    sortOrder: String(deal.sortOrder),
    published: deal.published,
  }
}

function toDealInput(state: FormState): DealInput {
  return {
    type: state.type,
    trimId: state.trimId,
    title: state.title,
    badge: state.badge || null,
    originalMonthly: toNumberOrNull(state.originalMonthly),
    monthlyPrice: toNumberOrNull(state.monthlyPrice) as number,
    leaseMonthly: toNumberOrNull(state.leaseMonthly),
    periodMonths: state.periodMonths,
    depositRate: toNumberOrNull(state.depositRate) ?? 0,
    prepayRate: toNumberOrNull(state.prepayRate) ?? 0,
    startsAt: fromDatetimeLocal(state.startsAt) as string,
    endsAt: fromDatetimeLocal(state.endsAt),
    sortOrder: toNumberOrNull(state.sortOrder) ?? 0,
    published: state.published,
  }
}

function DealForm({ deal }: { deal?: AdminDeal }) {
  const navigate = useNavigate()
  const [state, setState] = useState<FormState>(() => initialState(deal))
  const save = useSaveResource<DealInput, AdminDeal>('deals')
  const remove = useDeleteResource('deals')
  const errors = fieldErrorMap(save.error)
  const message = formErrorMessage(save.error)
  const set = <K extends keyof FormState>(key: K, value: FormState[K]) => setState((current) => ({ ...current, [key]: value }))
  const noDeposit = state.type === 'NO_DEPOSIT'

  return (
    <div className="space-y-4">
      <AdminPageHeader
        title={deal ? '특가 수정' : '특가 등록'}
        backTo="/admin/deals"
        actions={deal && <DisplayStatusBadge status={deal.displayStatus} />}
      />
      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault()
          save.mutate({ id: deal?.id, input: toDealInput(state) }, { onSuccess: () => navigate('/admin/deals') })
        }}
      >
        <section aria-label="차량" className="space-y-3 rounded-xl bg-white p-4 md:p-5">
          <h2 className="font-extrabold">차량</h2>
          <VehicleTrimPicker
            initialBrandId={deal?.vehicle.brandId}
            initialModelId={deal?.vehicle.modelId}
            trimId={state.trimId}
            onChange={(trimId) => set('trimId', trimId)}
            error={errors.trimId}
          />
        </section>

        <section aria-label="특가 정보" className="grid gap-3 rounded-xl bg-white p-4 sm:grid-cols-2 md:p-5 lg:grid-cols-4">
          <h2 className="font-extrabold sm:col-span-2 lg:col-span-4">특가 정보</h2>
          <SelectField label="유형" required value={state.type} error={errors.type} onChange={(e) => set('type', e.target.value as DealType)}>
            {(['TIME_SALE', 'NO_DEPOSIT'] as DealType[]).map((value) => (
              <option key={value} value={value}>
                {dealTypeLabel[value]}
              </option>
            ))}
          </SelectField>
          <TextField label="표시 제목" required className="sm:col-span-1 lg:col-span-2" value={state.title} error={errors.title} placeholder="예: 기아 쏘렌토 하이브리드" onChange={(e) => set('title', e.target.value)} />
          <TextField label="배지" value={state.badge} error={errors.badge} placeholder="예: HOT SALE, 마감임박" onChange={(e) => set('badge', e.target.value)} />
          <TextField label="월 납입료(원)" required inputMode="numeric" value={state.monthlyPrice} error={errors.monthlyPrice} onChange={(e) => set('monthlyPrice', e.target.value)} />
          <TextField label="정가 월 납입료(원)" inputMode="numeric" hint="입력하면 취소선과 할인율이 표시됩니다." value={state.originalMonthly} error={errors.originalMonthly} onChange={(e) => set('originalMonthly', e.target.value)} />
          <TextField label="리스 월 납입료(원)" inputMode="numeric" value={state.leaseMonthly} error={errors.leaseMonthly} onChange={(e) => set('leaseMonthly', e.target.value)} />
          <SelectField label="이용기간" required value={state.periodMonths} error={errors.periodMonths} onChange={(e) => set('periodMonths', Number(e.target.value))}>
            {[24, 36, 48, 60].map((months) => (
              <option key={months} value={months}>
                {months}개월
              </option>
            ))}
          </SelectField>
          <TextField label="보증금(%)" type="number" min={0} max={100} value={state.depositRate} error={errors.depositRate} hint={noDeposit ? '무보증특가는 보통 0' : undefined} onChange={(e) => set('depositRate', e.target.value)} />
          <TextField label="선납금(%)" type="number" min={0} max={100} value={state.prepayRate} error={errors.prepayRate} hint={noDeposit ? '무보증특가는 보통 0' : undefined} onChange={(e) => set('prepayRate', e.target.value)} />
        </section>

        <section aria-label="노출 설정" className="grid gap-3 rounded-xl bg-white p-4 sm:grid-cols-2 md:p-5 lg:grid-cols-4">
          <h2 className="font-extrabold sm:col-span-2 lg:col-span-4">노출 설정</h2>
          <TextField label="노출 시작" required type="datetime-local" value={state.startsAt} error={errors.startsAt} onChange={(e) => set('startsAt', e.target.value)} />
          <TextField label="노출 종료" type="datetime-local" hint="비우면 종료일 없이 계속 노출됩니다." value={state.endsAt} error={errors.endsAt} onChange={(e) => set('endsAt', e.target.value)} />
          <TextField label="정렬 순서" type="number" min={0} hint="작은 숫자가 먼저 보입니다." value={state.sortOrder} onChange={(e) => set('sortOrder', e.target.value)} />
          <ToggleField label="공개" description="끄면 사용자 화면에 보이지 않습니다." checked={state.published} onChange={(published) => set('published', published)} />
        </section>

        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>}
        <div className="flex flex-wrap justify-between gap-2">
          <Button type="submit" loading={save.isPending}>
            {deal ? '수정 저장' : '특가 등록'}
          </Button>
          {deal && (
            <Button
              variant="outline"
              className="text-red-600"
              loading={remove.isPending}
              onClick={() => {
                if (window.confirm('이 특가를 삭제할까요? 이 특가로 들어온 상담 신청 기록은 그대로 남습니다.')) {
                  remove.mutate(deal.id, { onSuccess: () => navigate('/admin/deals') })
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
