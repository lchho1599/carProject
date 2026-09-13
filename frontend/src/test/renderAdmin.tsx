import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { AdminLayout } from '../components/layout/AdminLayout'
import { AccountsPage } from '../pages/admin/AccountsPage'
import { AdminLoginPage } from '../pages/admin/AdminLoginPage'
import { BannerEditPage } from '../pages/admin/BannerEditPage'
import { BannersAdminPage } from '../pages/admin/BannersAdminPage'
import { DashboardPage } from '../pages/admin/DashboardPage'
import { DealEditPage } from '../pages/admin/DealEditPage'
import { DealsAdminPage } from '../pages/admin/DealsAdminPage'
import { LeadDetailPage } from '../pages/admin/LeadDetailPage'
import { LeadsPage } from '../pages/admin/LeadsPage'
import { VehiclesPage } from '../pages/admin/VehiclesPage'

export const adminMe = { id: 1, email: 'admin@test.local', name: '김관리', csrfToken: 'csrf-abc' }

/** 실제 관리자 라우트 구성으로 화면을 연다 */
export function renderAdmin(path: string) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  const router = createMemoryRouter(
    [
      { path: '/admin/login', element: <AdminLoginPage /> },
      {
        path: '/admin',
        element: <AdminLayout />,
        children: [
          { index: true, element: <DashboardPage /> },
          { path: 'leads', element: <LeadsPage /> },
          { path: 'leads/:leadId', element: <LeadDetailPage /> },
          { path: 'vehicles', element: <VehiclesPage /> },
          { path: 'deals', element: <DealsAdminPage /> },
          { path: 'deals/:dealId', element: <DealEditPage /> },
          { path: 'banners', element: <BannersAdminPage /> },
          { path: 'banners/:bannerId', element: <BannerEditPage /> },
          { path: 'accounts', element: <AccountsPage /> },
        ],
      },
    ],
    { initialEntries: [path] },
  )
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
  return { user: userEvent.setup(), router }
}
