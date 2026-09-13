import { describe, expect, it } from 'vitest'
import { newPasswordError, passwordRuleError } from './password'

describe('관리자 비밀번호 규칙 (서버와 동일)', () => {
  it.each(['abcd1234', 'Rent2026db', '한글비번1234'])('%s 는 통과', (password) => {
    expect(passwordRuleError(password)).toBeNull()
  })

  it.each(['short1', 'onlyletters', '12345678', ' spaced12 ', 'a1'.repeat(51)])('%s 는 거부', (password) => {
    expect(passwordRuleError(password)).not.toBeNull()
  })

  it('확인 값이 다르면 알려준다', () => {
    expect(newPasswordError('abcd1234', 'abcd1235')).toBe('새 비밀번호와 확인 값이 다릅니다.')
    expect(newPasswordError('abcd1234', 'abcd1234')).toBeNull()
  })
})
