import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from 'react'
import { useId } from 'react'
import { cn } from '../../lib/cn'
import { adminInputClass } from './formUtils'

// 관리자 입력 폼 공통 부품 — 서버 검증 오류(fieldErrors)를 항목 아래에 표시한다 (함수는 formUtils.ts)

type FieldProps = {
  label: string
  error?: string
  hint?: string
  required?: boolean
  className?: string
  children: (id: string) => ReactNode
}

export function Field({ label, error, hint, required, className, children }: FieldProps) {
  const id = useId()
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1 block text-sm font-semibold text-gray-700">
        {label}
        {required && <span className="ml-0.5 text-accent-600">*</span>}
      </label>
      {children(id)}
      {hint && !error && <p className="mt-1 text-xs text-gray-500">{hint}</p>}
      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  )
}

export function TextField({
  label,
  error,
  hint,
  required,
  className,
  ...input
}: Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> & { label: string; error?: string; hint?: string }) {
  return (
    <Field label={label} error={error} hint={hint} required={required} className={className}>
      {(id) => (
        <input id={id} aria-invalid={!!error} className={cn(adminInputClass, error ? 'border-red-400' : 'border-gray-300')} {...input} />
      )}
    </Field>
  )
}

export function SelectField({
  label,
  error,
  hint,
  required,
  className,
  children,
  ...select
}: Omit<SelectHTMLAttributes<HTMLSelectElement>, 'id'> & { label: string; error?: string; hint?: string }) {
  return (
    <Field label={label} error={error} hint={hint} required={required} className={className}>
      {(id) => (
        <select id={id} aria-invalid={!!error} className={cn(adminInputClass, error ? 'border-red-400' : 'border-gray-300')} {...select}>
          {children}
        </select>
      )}
    </Field>
  )
}

/** 공개/사용 여부 스위치 */
export function ToggleField({
  label,
  checked,
  onChange,
  description,
}: {
  label: string
  checked: boolean
  onChange: (checked: boolean) => void
  description?: string
}) {
  return (
    <label className="flex cursor-pointer items-center justify-between gap-3 rounded-lg border border-gray-200 bg-white px-4 py-3">
      <span>
        <span className="block text-sm font-semibold text-gray-700">{label}</span>
        {description && <span className="block text-xs text-gray-500">{description}</span>}
      </span>
      <input
        type="checkbox"
        role="switch"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
        className="peer sr-only"
      />
      <span
        aria-hidden="true"
        className="relative h-6 w-11 shrink-0 rounded-full bg-gray-300 transition-colors after:absolute after:top-0.5 after:left-0.5 after:size-5 after:rounded-full after:bg-white after:transition-transform peer-checked:bg-primary peer-checked:after:translate-x-5 peer-focus-visible:outline-2 peer-focus-visible:outline-accent"
      />
    </label>
  )
}
