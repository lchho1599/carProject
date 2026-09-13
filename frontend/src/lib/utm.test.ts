import { describe, expect, it } from 'vitest'
import { captureUtm, getUtm } from './utm'

describe('광고 파라미터(utm) 저장', () => {
  it('utm_ 로 시작하는 값만 저장한다', () => {
    captureUtm('?utm_source=naver&utm_campaign=sorento&page=2')
    expect(getUtm()).toEqual({ utm_source: 'naver', utm_campaign: 'sorento' })
  })

  it('첫 유입 값을 유지하고 이후 방문 값으로 덮어쓰지 않는다', () => {
    captureUtm('?utm_source=naver')
    captureUtm('?utm_source=google')
    expect(getUtm()).toEqual({ utm_source: 'naver' })
  })

  it('utm 이 없으면 저장하지 않는다', () => {
    captureUtm('?page=1')
    expect(getUtm()).toBeUndefined()
  })
})
