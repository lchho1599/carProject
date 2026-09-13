import '@testing-library/jest-dom/vitest'
import { cleanup, configure } from '@testing-library/react'
import { afterEach } from 'vitest'

// 전체 테스트를 병렬로 돌리면 PC 부하로 화면 갱신이 늦어질 수 있어 findBy/waitFor 기본 대기(1초)를 늘린다
configure({ asyncUtilTimeout: 3000 })

afterEach(() => {
  cleanup()
  sessionStorage.clear()
})
