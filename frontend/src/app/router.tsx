import { createBrowserRouter } from 'react-router'
import { AdminLayout } from '../components/layout/AdminLayout'
import { PublicLayout } from '../components/layout/PublicLayout'
import { AccountsPage } from '../pages/admin/AccountsPage'
import { AdminLoginPage } from '../pages/admin/AdminLoginPage'
import { BannerEditPage } from '../pages/admin/BannerEditPage'
import { BannersAdminPage } from '../pages/admin/BannersAdminPage'
import { DashboardPage } from '../pages/admin/DashboardPage'
import { DealEditPage } from '../pages/admin/DealEditPage'
import { DealsAdminPage } from '../pages/admin/DealsAdminPage'
import { InstantAdminPage } from '../pages/admin/InstantAdminPage'
import { InstantEditPage } from '../pages/admin/InstantEditPage'
import { LeadDetailPage } from '../pages/admin/LeadDetailPage'
import { LeadsPage } from '../pages/admin/LeadsPage'
import { ModelEditPage } from '../pages/admin/ModelEditPage'
import { VehiclesPage } from '../pages/admin/VehiclesPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { CompletePage } from '../pages/public/CompletePage'
import { DealsPage } from '../pages/public/DealsPage'
import { EstimateDetailPage } from '../pages/public/EstimateDetailPage'
import { EstimatePage } from '../pages/public/EstimatePage'
import { HomePage } from '../pages/public/HomePage'
import { InstantPage } from '../pages/public/InstantPage'
import { PolicyPage } from '../pages/public/PolicyPage'

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'estimate', element: <EstimatePage /> },
      { path: 'estimate/:modelId', element: <EstimateDetailPage /> },
      { path: 'deals', element: <DealsPage /> },
      { path: 'instant', element: <InstantPage /> },
      { path: 'complete', element: <CompletePage /> },
      { path: 'terms', element: <PolicyPage title="이용약관" /> },
      { path: 'privacy', element: <PolicyPage title="개인정보처리방침" /> },
    ],
  },
  { path: 'admin/login', element: <AdminLoginPage /> },
  {
    path: 'admin',
    element: <AdminLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'leads', element: <LeadsPage /> },
      { path: 'leads/:leadId', element: <LeadDetailPage /> },
      { path: 'vehicles', element: <VehiclesPage /> },
      { path: 'vehicles/models/:modelId', element: <ModelEditPage /> },
      { path: 'deals', element: <DealsAdminPage /> },
      { path: 'deals/:dealId', element: <DealEditPage /> },
      { path: 'instant', element: <InstantAdminPage /> },
      { path: 'instant/:stockId', element: <InstantEditPage /> },
      { path: 'banners', element: <BannersAdminPage /> },
      { path: 'banners/:bannerId', element: <BannerEditPage /> },
      { path: 'accounts', element: <AccountsPage /> },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])
