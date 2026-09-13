import { Outlet, ScrollRestoration } from 'react-router'
import { ConsultModalProvider } from '../lead/ConsultModal'
import { FloatingConsultButton } from './FloatingConsultButton'
import { Footer } from './Footer'
import { Header } from './Header'
import { MobileBottomBar } from './MobileBottomBar'

export function PublicLayout() {
  return (
    <ConsultModalProvider>
      {/* overflow-x-clip: 어떤 요소가 넘치더라도 모바일에서 화면이 옆으로 밀리지 않게 하는 안전장치 (sticky 헤더에 영향 없음) */}
      <div className="flex min-h-dvh flex-col overflow-x-clip pb-bottom-bar md:pb-0">
        <Header />
        <main className="flex-1">
          <Outlet />
        </main>
        <Footer />
      </div>
      <MobileBottomBar />
      <FloatingConsultButton />
      <ScrollRestoration />
    </ConsultModalProvider>
  )
}
