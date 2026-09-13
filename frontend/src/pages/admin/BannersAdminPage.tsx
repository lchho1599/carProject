import { Link } from 'react-router'
import { AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { DisplayStatusBadge } from '../../components/admin/DisplayStatusBadge'
import { ErrorState } from '../../components/ui/StateViews'
import { useAdminBanners } from '../../features/admin/catalog'
import { formatDateTime } from '../../lib/format'

export function BannersAdminPage() {
  const { data, isPending, isError, refetch } = useAdminBanners()

  return (
    <div className="space-y-4">
      <AdminPageHeader
        title="배너 관리"
        actions={
          <Link to="/admin/banners/new" className="inline-flex h-9 items-center rounded-lg bg-primary px-3 text-sm font-semibold text-white hover:bg-primary-800">
            배너 등록
          </Link>
        }
      />
      <p className="text-sm text-gray-500">메인 화면 상단 슬라이드 배너입니다. 정렬 순서가 작은 배너부터 보입니다.</p>

      {isPending ? (
        <AdminLoading />
      ) : isError ? (
        <ErrorState onRetry={() => refetch()} />
      ) : data.length === 0 ? (
        <div className="rounded-xl bg-white py-12 text-center text-gray-500">등록된 배너가 없습니다.</div>
      ) : (
        <ul className="grid gap-3 md:grid-cols-2">
          {data.map((banner) => (
            <li key={banner.id}>
              <Link to={`/admin/banners/${banner.id}`} className="block overflow-hidden rounded-xl bg-white hover:ring-2 hover:ring-primary-200">
                <img src={banner.imagePcUrl} alt={`${banner.title} PC 이미지`} className="aspect-[3/1] w-full bg-gray-100 object-cover" />
                <div className="space-y-1 p-4">
                  <div className="flex items-center justify-between gap-2">
                    <strong className="truncate">{banner.title}</strong>
                    <DisplayStatusBadge status={banner.displayStatus} />
                  </div>
                  <p className="truncate text-xs text-gray-500">링크: {banner.linkUrl ?? '없음'}</p>
                  <p className="text-xs text-gray-500">
                    {formatDateTime(banner.startsAt)} ~ {banner.endsAt ? formatDateTime(banner.endsAt) : '종료일 없음'} · 순서 {banner.sortOrder}
                  </p>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
