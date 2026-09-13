import { useState } from 'react'
import { cn } from '../../lib/cn'
import { CarIcon } from '../ui/icons'

type Props = {
  src: string | null
  alt: string
  className?: string
}

/** 차량 이미지 — 이미지가 없거나 불러오지 못하면 차량 아이콘 자리표시를 보여준다 */
export function VehicleImage({ src, alt, className }: Props) {
  const [failed, setFailed] = useState(false)
  const showImage = src && !failed

  return (
    <div
      className={cn(
        'flex aspect-[16/10] items-center justify-center overflow-hidden bg-gradient-to-br from-primary-50 to-gray-100',
        className,
      )}
    >
      {showImage ? (
        <img src={src} alt={alt} loading="lazy" onError={() => setFailed(true)} className="h-full w-full object-contain" />
      ) : (
        <div className="flex flex-col items-center gap-1 text-primary-300" role="img" aria-label={alt}>
          <CarIcon className="size-14" />
          <span className="text-xs font-semibold text-primary-300">이미지 준비 중</span>
        </div>
      )}
    </div>
  )
}
