import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import type { Banner } from '../../features/banner/api'
import { cn } from '../../lib/cn'

const AUTO_SLIDE_MS = 5000

/** 메인 배너 — 5초마다 자동 전환, 점·화살표로 이동, 마우스를 올리면 일시정지 */
export function BannerSlider({ banners }: { banners: Banner[] }) {
  const [index, setIndex] = useState(0)
  const [paused, setPaused] = useState(false)
  const count = banners.length

  useEffect(() => {
    if (count <= 1 || paused) return
    const timer = window.setInterval(() => setIndex((current) => (current + 1) % count), AUTO_SLIDE_MS)
    return () => window.clearInterval(timer)
  }, [count, paused])

  if (count === 0) return null
  const go = (next: number) => setIndex((next + count) % count)

  return (
    <section
      aria-roledescription="carousel"
      aria-label="이벤트 배너"
      // 높이는 이미지에 맞춘다 (메인에서 세로 flex 칸 안에 두어 옆 폼 높이만큼 늘어나지 않음 → 화살표·점이 이미지 위에 놓임)
      className="relative overflow-hidden rounded-2xl"
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
    >
      <div className="flex transition-transform duration-500" style={{ transform: `translateX(-${index * 100}%)` }}>
        {banners.map((banner, i) => (
          <div
            key={banner.id}
            className="w-full shrink-0"
            aria-roledescription="slide"
            aria-label={`${i + 1} / ${count}`}
            aria-hidden={i !== index}
          >
            <BannerSlide banner={banner} order={i} />
          </div>
        ))}
      </div>

      {count > 1 && (
        <>
          <button
            type="button"
            aria-label="이전 배너"
            onClick={() => go(index - 1)}
            className="absolute top-1/2 left-3 hidden size-10 -translate-y-1/2 items-center justify-center rounded-full bg-black/30 text-xl text-white hover:bg-black/50 md:flex"
          >
            ‹
          </button>
          <button
            type="button"
            aria-label="다음 배너"
            onClick={() => go(index + 1)}
            className="absolute top-1/2 right-3 hidden size-10 -translate-y-1/2 items-center justify-center rounded-full bg-black/30 text-xl text-white hover:bg-black/50 md:flex"
          >
            ›
          </button>
          <div className="absolute bottom-3 left-1/2 flex -translate-x-1/2 gap-1.5">
            {banners.map((banner, i) => (
              <button
                key={banner.id}
                type="button"
                aria-label={`${i + 1}번 배너 보기`}
                aria-current={i === index}
                onClick={() => go(i)}
                className={cn('h-2 rounded-full transition-all', i === index ? 'w-6 bg-white' : 'w-2 bg-white/50')}
              />
            ))}
          </div>
        </>
      )}
    </section>
  )
}

const FALLBACK_BACKGROUNDS = [
  'from-primary-800 to-primary-500',
  'from-accent-700 to-accent-500',
  'from-gray-900 to-primary-700',
]

/** 배너 이미지가 없거나 깨지면 제목을 넣은 기본 디자인으로 대신 보여준다 */
function BannerSlide({ banner, order }: { banner: Banner; order: number }) {
  const [imageFailed, setImageFailed] = useState(false)

  const content = imageFailed ? (
    <div
      className={cn(
        'flex aspect-[16/9] flex-col justify-center bg-gradient-to-br px-6 text-white md:aspect-[3/1] md:px-12',
        FALLBACK_BACKGROUNDS[order % FALLBACK_BACKGROUNDS.length],
      )}
    >
      <p className="text-sm font-semibold opacity-80">신차 장기렌트 · 리스</p>
      <p className="mt-2 text-2xl font-extrabold md:text-4xl">{banner.title}</p>
      {banner.linkUrl && <p className="mt-4 text-sm font-semibold underline underline-offset-4">자세히 보기</p>}
    </div>
  ) : (
    <picture>
      <source media="(min-width: 768px)" srcSet={banner.imagePcUrl} />
      <img
        src={banner.imageMobileUrl}
        alt={banner.title}
        onError={() => setImageFailed(true)}
        className="block aspect-[16/9] w-full object-cover md:aspect-[3/1]"
      />
    </picture>
  )

  return banner.linkUrl ? (
    <Link to={banner.linkUrl} className="block">
      {content}
    </Link>
  ) : (
    content
  )
}
