/// <reference types="vitest/config" />
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendUrl = env.VITE_DEV_BACKEND_URL || 'http://localhost:8080'

  return {
    plugins: [react(), tailwindcss()],
    server: {
      // 기본은 이 PC 에서만 접속. DEV_LAN=true 로 실행하면 같은 Wi-Fi 의 휴대폰에서도 접속 가능 (scripts\frontend-start-mobile.bat)
      host: process.env.DEV_LAN === 'true' ? true : 'localhost',
      port: Number(env.FRONTEND_PORT || 5173),
      strictPort: true,
      // 로컬 개발: /api 요청을 Spring Boot로 전달 (CORS 설정 불필요)
      proxy: {
        '/api': { target: backendUrl, changeOrigin: true },
      },
    },
    test: {
      environment: 'jsdom',
      // 입력을 여러 번 하는 긴 화면 시나리오가 병렬 실행 시 5초(기본)를 넘길 수 있어 늘린다
      testTimeout: 15_000,
      setupFiles: ['./src/test/setup.ts'],
      css: false,
    },
  }
})
