import { describe, expect, it } from 'vitest'
import { ApiError } from '../../lib/apiClient'
import { fieldErrorMap, formErrorMessage, toNumberOrNull } from './formUtils'

describe('관리자 폼 함수', () => {
  it('숫자 입력: 쉼표 허용, 빈 값·잘못된 값은 null', () => {
    expect(toNumberOrNull('1,200,000')).toBe(1200000)
    expect(toNumberOrNull('0')).toBe(0)
    expect(toNumberOrNull('')).toBeNull()
    expect(toNumberOrNull('abc')).toBeNull()
  })

  it('서버 항목 오류를 필드별로 모으고, 전체 메시지를 정한다', () => {
    const fieldError = new ApiError(400, 'VALIDATION_FAILED', '요청 값 오류', [{ field: 'linkUrl', message: '주소 형식 오류' }])
    expect(fieldErrorMap(fieldError)).toEqual({ linkUrl: '주소 형식 오류' })
    expect(formErrorMessage(fieldError)).toBe('입력값을 확인해 주세요.')

    const conflict = new ApiError(409, 'DUPLICATED', '이미 등록된 브랜드명입니다.')
    expect(formErrorMessage(conflict)).toBe('이미 등록된 브랜드명입니다.')
    expect(formErrorMessage(null)).toBeNull()
  })
})
