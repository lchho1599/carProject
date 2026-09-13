import { Link } from 'react-router'
import { CheckCircleIcon } from '../../components/ui/icons'
import { useSite } from '../../features/site/api'

export function CompletePage() {
  const { data: siteInfo } = useSite()

  return (
    <section className="mx-auto flex max-w-md flex-col items-center px-4 py-16 text-center">
      <CheckCircleIcon className="mb-4 size-16 text-accent" />
      <h1 className="text-2xl font-extrabold">상담 신청이 접수되었습니다</h1>
      <p className="mt-3 text-gray-600">
        담당 상담사가 확인 후 빠르게 연락드리겠습니다.
        <br />
        조건에 맞는 최저가 견적을 준비해 드릴게요.
      </p>
      {siteInfo?.phone && (
        <p className="mt-4 text-sm text-gray-500">
          급하신 경우 대표번호{' '}
          <a href={`tel:${siteInfo.phone}`} className="font-bold text-primary">
            {siteInfo.phone}
          </a>
          로 연락 주세요.
        </p>
      )}
      <Link to="/" className="mt-8 inline-flex h-12 items-center rounded-lg bg-primary px-6 font-bold text-white">
        메인으로
      </Link>
    </section>
  )
}
