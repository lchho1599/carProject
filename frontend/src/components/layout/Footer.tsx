import { Link } from 'react-router'
import { site } from '../../config/site'
import { useSite } from '../../features/site/api'

export function Footer() {
  const { data: siteInfo } = useSite()
  const siteName = siteInfo?.name ?? site.name

  return (
    <footer className="border-t border-gray-200 bg-white text-sm text-gray-500">
      <div className="mx-auto max-w-6xl space-y-3 px-4 py-8">
        <div className="flex gap-4 font-semibold text-gray-700">
          <Link to="/terms">이용약관</Link>
          <Link to="/privacy">개인정보처리방침</Link>
        </div>
        {siteInfo && (
          <dl className="flex flex-wrap gap-x-4 gap-y-1">
            <Info label="상호" value={siteInfo.businessName} />
            <Info label="대표" value={siteInfo.representative} />
            <Info label="사업자등록번호" value={siteInfo.businessNumber} />
            <Info label="주소" value={siteInfo.address} />
            <Info label="대표번호" value={siteInfo.phone} />
          </dl>
        )}
        <p>© {siteName}. All rights reserved.</p>
      </div>
    </footer>
  )
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex gap-1">
      <dt>{label}</dt>
      <dd className="text-gray-700">{value}</dd>
    </div>
  )
}
