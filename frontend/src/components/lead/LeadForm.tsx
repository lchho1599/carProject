import { zodResolver } from '@hookform/resolvers/zod'
import { useId, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { useCreateLead } from '../../features/lead/api'
import { leadFormDefaults, leadFormSchema, type LeadFormValues } from '../../features/lead/schema'
import type { LeadContext } from '../../features/lead/types'
import { ApiError } from '../../lib/apiClient'
import { cn } from '../../lib/cn'
import { formatPhoneInput } from '../../lib/format'
import { trackLeadSubmitted } from '../../lib/tracking'
import { getUtm } from '../../lib/utm'
import { Button } from '../ui/Button'
import { AgreementModal } from './AgreementModal'

type Props = {
  context: LeadContext
  submitLabel?: string
  /** 신청 성공 직후 호출 (예: 모달 닫기). 이후 완료 화면으로 이동한다. */
  onSuccess?: () => void
  className?: string
}

const FORM_FIELDS = ['name', 'phone', 'agreePrivacy'] as const

/**
 * 상담신청 공통 폼 — 이름·연락처·동의만 입력받고, 차량·조건 정보는 context로 받아 함께 보낸다.
 */
export function LeadForm({ context, submitLabel = '무료 상담 신청', onSuccess, className }: Props) {
  const navigate = useNavigate()
  const createLead = useCreateLead()
  // 한 화면에 폼이 여러 개(메인 폼 + 상담 모달) 있어도 라벨·오류 연결이 겹치지 않도록 고유 id 사용
  const uid = useId()
  const ids = { name: `${uid}-name`, phone: `${uid}-phone` }
  const [agreementOpen, setAgreementOpen] = useState(false)
  const [formMessage, setFormMessage] = useState<string | null>(null)

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<LeadFormValues>({
    resolver: zodResolver(leadFormSchema),
    defaultValues: leadFormDefaults,
  })

  const onSubmit = handleSubmit(async (values) => {
    setFormMessage(null)
    const { label: _label, ...target } = context
    try {
      await createLead.mutateAsync({
        ...target,
        name: values.name,
        phone: values.phone,
        agreePrivacy: values.agreePrivacy,
        agreeMarketing: values.agreeMarketing,
        website: values.website,
        sourceUrl: window.location.pathname + window.location.search,
        utm: getUtm(),
      })
      trackLeadSubmitted(context.type)
      onSuccess?.()
      navigate('/complete', { state: { type: context.type } })
    } catch (error) {
      if (!(error instanceof ApiError)) {
        setFormMessage('일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.')
        return
      }
      const unhandled = error.fieldErrors.filter((fieldError) => {
        const field = FORM_FIELDS.find((name) => name === fieldError.field)
        if (field) setError(field, { message: fieldError.message })
        return !field
      })
      if (error.fieldErrors.length === 0 || unhandled.length > 0) {
        setFormMessage(error.message)
      }
    }
  })

  return (
    <form onSubmit={onSubmit} noValidate className={cn('space-y-4', className)}>
      <div>
        <label htmlFor={ids.name} className="mb-1 block text-sm font-semibold text-gray-700">
          이름
        </label>
        <input
          id={ids.name}
          type="text"
          autoComplete="name"
          placeholder="홍길동"
          aria-invalid={!!errors.name}
          aria-describedby={errors.name ? `${ids.name}-error` : undefined}
          className={inputClass(!!errors.name)}
          {...register('name')}
        />
        {errors.name && <FieldMessage id={`${ids.name}-error`}>{errors.name.message}</FieldMessage>}
      </div>

      <div>
        <label htmlFor={ids.phone} className="mb-1 block text-sm font-semibold text-gray-700">
          연락처
        </label>
        <Controller
          name="phone"
          control={control}
          render={({ field }) => (
            <input
              id={ids.phone}
              type="tel"
              inputMode="numeric"
              autoComplete="tel"
              placeholder="010-0000-0000"
              aria-invalid={!!errors.phone}
              aria-describedby={errors.phone ? `${ids.phone}-error` : undefined}
              className={inputClass(!!errors.phone)}
              name={field.name}
              ref={field.ref}
              value={field.value}
              onBlur={field.onBlur}
              onChange={(event) => field.onChange(formatPhoneInput(event.target.value))}
            />
          )}
        />
        {errors.phone && <FieldMessage id={`${ids.phone}-error`}>{errors.phone.message}</FieldMessage>}
      </div>

      {/* 스팸 방지 숨김 필드 — 사람에게는 보이지 않는다 */}
      <div aria-hidden="true" className="absolute -left-[9999px] h-0 w-0 overflow-hidden">
        <label>
          웹사이트
          <input type="text" tabIndex={-1} autoComplete="off" {...register('website')} />
        </label>
      </div>

      <div className="space-y-2 rounded-lg bg-gray-50 p-3 text-sm">
        <div className="flex items-center justify-between gap-2">
          <label className="flex cursor-pointer items-center gap-2">
            <input type="checkbox" className="size-4 accent-primary" {...register('agreePrivacy')} />
            <span>
              <span className="text-accent-600">[필수]</span> 개인정보 수집·이용 동의
            </span>
          </label>
          <button
            type="button"
            onClick={() => setAgreementOpen(true)}
            className="shrink-0 text-gray-500 underline underline-offset-2"
          >
            보기
          </button>
        </div>
        {errors.agreePrivacy && <FieldMessage>{errors.agreePrivacy.message}</FieldMessage>}
        <label className="flex cursor-pointer items-center gap-2">
          <input type="checkbox" className="size-4 accent-primary" {...register('agreeMarketing')} />
          <span>
            <span className="text-gray-500">[선택]</span> 특가·이벤트 소식 받기
          </span>
        </label>
      </div>

      {formMessage && (
        <p role="alert" className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
          {formMessage}
        </p>
      )}

      <Button type="submit" variant="accent" size="lg" fullWidth loading={createLead.isPending}>
        {submitLabel}
      </Button>

      <AgreementModal open={agreementOpen} onClose={() => setAgreementOpen(false)} />
    </form>
  )
}

function inputClass(invalid: boolean) {
  return cn(
    'h-12 w-full rounded-lg border bg-white px-3 text-base placeholder:text-gray-400 focus:border-primary focus:outline-none',
    invalid ? 'border-red-400' : 'border-gray-300',
  )
}

function FieldMessage({ id, children }: { id?: string; children: React.ReactNode }) {
  return (
    <p id={id} className="mt-1 text-sm text-red-600">
      {children}
    </p>
  )
}
