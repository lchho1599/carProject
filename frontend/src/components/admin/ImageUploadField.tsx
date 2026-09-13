import { useId, useRef } from 'react'
import { useUploadImage } from '../../features/admin/catalog'
import { ApiError } from '../../lib/apiClient'
import { cn } from '../../lib/cn'
import { Button } from '../ui/Button'

type Props = {
  label: string
  value: string | null
  onChange: (url: string | null) => void
  error?: string
  hint?: string
  required?: boolean
  /** 미리보기 비율 (예: aspect-[3/1]) */
  previewClassName?: string
}

/** 이미지 업로드 칸 — 파일을 고르면 바로 업로드하고 공개 주소를 값으로 쓴다 */
export function ImageUploadField({ label, value, onChange, error, hint, required, previewClassName = 'aspect-[16/10]' }: Props) {
  const inputId = useId()
  const inputRef = useRef<HTMLInputElement>(null)
  const upload = useUploadImage()

  const selectFile = (file: File | undefined) => {
    if (!file) return
    upload.mutate(file, {
      onSuccess: (result) => onChange(result.url),
      onSettled: () => {
        if (inputRef.current) inputRef.current.value = ''
      },
    })
  }

  const uploadError = upload.error instanceof ApiError ? upload.error.message : upload.error ? '업로드하지 못했습니다.' : null

  return (
    <div>
      <label htmlFor={inputId} className="mb-1 block text-sm font-semibold text-gray-700">
        {label}
        {required && <span className="ml-0.5 text-accent-600">*</span>}
      </label>
      <div
        className={cn(
          'flex items-center justify-center overflow-hidden rounded-lg border bg-gray-50',
          previewClassName,
          error ? 'border-red-400' : 'border-gray-200',
        )}
      >
        {value ? (
          <img src={value} alt={`${label} 미리보기`} className="h-full w-full object-contain" />
        ) : (
          <span className="text-sm text-gray-400">이미지 없음</span>
        )}
      </div>
      <div className="mt-2 flex items-center gap-2">
        <input
          ref={inputRef}
          id={inputId}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          className="sr-only"
          onChange={(event) => selectFile(event.target.files?.[0])}
        />
        <Button size="sm" variant="outline" loading={upload.isPending} onClick={() => inputRef.current?.click()}>
          {value ? '이미지 변경' : '이미지 선택'}
        </Button>
        {value && !required && (
          <Button size="sm" variant="ghost" onClick={() => onChange(null)}>
            삭제
          </Button>
        )}
      </div>
      {hint && !error && !uploadError && <p className="mt-1 text-xs text-gray-500">{hint}</p>}
      {(uploadError || error) && <p className="mt-1 text-sm text-red-600">{uploadError ?? error}</p>}
    </div>
  )
}
