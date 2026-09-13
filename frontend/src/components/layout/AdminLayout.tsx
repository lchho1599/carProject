import { useState } from 'react'
import { Link, Navigate, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import { site } from '../../config/site'
import { useAdminLogout, useAdminMe } from '../../features/admin/auth'
import { ApiError } from '../../lib/apiClient'
import { cn } from '../../lib/cn'
import { useNoIndex } from '../../lib/useNoIndex'
import { Button } from '../ui/Button'
import { Spinner } from '../ui/Spinner'

const MENU: { to: string; label: string; end?: boolean; ready: boolean }[] = [
  { to: '/admin', label: '대시보드', end: true, ready: true },
  { to: '/admin/leads', label: '상담 신청', ready: true },
  { to: '/admin/vehicles', label: '차량 관리', ready: true },
  { to: '/admin/deals', label: '특가 관리', ready: true },
  { to: '/admin/instant', label: '즉시출고 관리', ready: true },
  { to: '/admin/banners', label: '배너 관리', ready: true },
  { to: '/admin/accounts', label: '관리자 계정', ready: true },
  // 알림 수신 메일 등 — 기능 개발 마지막에 구현
  { to: '/admin/settings', label: '설정', ready: false },
]

/**
 * 관리자 레이아웃 — 로그인 확인(인증 가드) 후에만 하위 화면을 보여준다.
 * 로그인하지 않았으면 /admin/login?redirect=현재주소 로 보낸다.
 */
export function AdminLayout() {
  const { data: me, isPending, error } = useAdminMe()
  const location = useLocation()
  const navigate = useNavigate()
  const logout = useAdminLogout()
  // 모바일 메뉴는 연 화면의 주소를 기억해, 다른 화면으로 이동하면 자동으로 닫힌 것으로 본다
  const [menuOpenedAt, setMenuOpenedAt] = useState<string | null>(null)
  const menuOpen = menuOpenedAt === location.pathname
  const setMenuOpen = (updater: (open: boolean) => boolean) =>
    setMenuOpenedAt(updater(menuOpen) ? location.pathname : null)

  useNoIndex()

  if (isPending) {
    return (
      <div className="flex min-h-dvh items-center justify-center text-primary">
        <Spinner className="size-8" />
      </div>
    )
  }

  if (error || !me) {
    if (error instanceof ApiError && error.status !== 401) {
      return (
        <div className="flex min-h-dvh items-center justify-center p-4 text-center text-gray-600">
          관리자 정보를 불러오지 못했습니다. 잠시 후 새로고침해 주세요.
        </div>
      )
    }
    const redirect = encodeURIComponent(location.pathname + location.search)
    return <Navigate to={`/admin/login?redirect=${redirect}`} replace />
  }

  const handleLogout = () => {
    logout.mutate(undefined, { onSettled: () => navigate('/admin/login', { replace: true }) })
  }

  const menu = (
    <nav aria-label="관리자 메뉴" className="space-y-1 p-3">
      {MENU.map((item) =>
        item.ready ? (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              cn(
                'block rounded-lg px-3 py-2.5 text-sm font-semibold',
                isActive ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100',
              )
            }
          >
            {item.label}
          </NavLink>
        ) : (
          <span
            key={item.to}
            aria-disabled="true"
            className="flex items-center justify-between rounded-lg px-3 py-2.5 text-sm font-semibold text-gray-400"
          >
            {item.label}
            <span className="rounded bg-gray-100 px-1.5 py-0.5 text-[10px]">준비중</span>
          </span>
        ),
      )}
    </nav>
  )

  return (
    <div className="min-h-dvh bg-gray-100 lg:grid lg:grid-cols-[240px_1fr]">
      {/* PC 사이드바 */}
      <aside className="hidden border-r border-gray-200 bg-white lg:block">
        <div className="sticky top-0">
          <Link to="/admin" className="block border-b border-gray-100 px-5 py-4 font-extrabold text-primary">
            {site.name} 관리자
          </Link>
          {menu}
        </div>
      </aside>

      <div className="min-w-0">
        <header className="sticky top-0 z-30 flex h-14 items-center justify-between gap-3 border-b border-gray-200 bg-white px-4">
          <div className="flex items-center gap-2 lg:hidden">
            <button
              type="button"
              aria-label="메뉴 열기"
              aria-expanded={menuOpen}
              onClick={() => setMenuOpen((open) => !open)}
              className="flex size-9 items-center justify-center rounded-lg text-xl hover:bg-gray-100"
            >
              ☰
            </button>
            <Link to="/admin" className="font-extrabold text-primary">
              관리자
            </Link>
          </div>
          <div className="hidden lg:block" />
          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-600">
              <strong className="text-gray-900">{me.name}</strong>님
            </span>
            <Button variant="outline" size="sm" onClick={handleLogout} loading={logout.isPending}>
              로그아웃
            </Button>
          </div>
        </header>

        {menuOpen && <div className="border-b border-gray-200 bg-white lg:hidden">{menu}</div>}

        <main className="mx-auto max-w-7xl p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
