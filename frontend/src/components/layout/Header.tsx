import { Link, NavLink, useLocation } from 'react-router'
import { mainMenu, site } from '../../config/site'
import { useSite } from '../../features/site/api'
import { cn } from '../../lib/cn'
import { useConsultModal } from '../lead/consultModalContext'
import { Button } from '../ui/Button'
import { PhoneIcon } from '../ui/icons'

export function Header() {
  const { data: siteInfo } = useSite()
  const { openConsult } = useConsultModal()
  const { pathname } = useLocation()
  const siteName = siteInfo?.name ?? site.name

  return (
    <header className="sticky top-0 z-40 border-b border-gray-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between gap-4 px-4 md:h-16">
        <Link to="/" className="text-lg font-extrabold tracking-tight text-primary md:text-xl">
          {siteName}
        </Link>

        <nav aria-label="주 메뉴" className="hidden items-center gap-1 md:flex">
          {mainMenu.map((item) => {
            const active = pathname.startsWith('match' in item ? item.match : item.to)
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={cn(
                  'rounded-lg px-4 py-2 font-semibold transition-colors',
                  active ? 'text-primary' : 'text-gray-600 hover:text-primary',
                )}
              >
                {item.label}
              </NavLink>
            )
          })}
        </nav>

        <div className="flex items-center gap-2">
          {siteInfo?.phone && (
            <a
              href={`tel:${siteInfo.phone}`}
              className="flex items-center gap-1.5 rounded-lg px-2 py-2 font-bold text-primary hover:bg-primary-50"
              aria-label={`전화 상담 ${siteInfo.phone}`}
            >
              <PhoneIcon className="size-5" />
              <span className="hidden lg:inline">{siteInfo.phone}</span>
            </a>
          )}
          <Button variant="accent" size="sm" className="hidden md:inline-flex" onClick={() => openConsult()}>
            상담신청
          </Button>
        </div>
      </div>
    </header>
  )
}
