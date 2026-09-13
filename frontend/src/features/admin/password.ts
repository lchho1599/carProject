// 관리자 비밀번호 규칙 — 백엔드 PasswordPolicy 와 같다 (서버에서도 다시 검사)

export const PASSWORD_RULE = '8~100자, 영문과 숫자를 모두 포함'

/** 규칙에 맞지 않으면 안내 문구, 맞으면 null */
export function passwordRuleError(password: string): string | null {
  if (
    password.length < 8 ||
    password.length > 100 ||
    !/\p{L}/u.test(password) ||
    !/\d/.test(password) ||
    password !== password.trim()
  ) {
    return '비밀번호는 8~100자이며 영문과 숫자를 모두 포함해야 합니다.'
  }
  return null
}

/** 새 비밀번호 + 확인 입력 검사 */
export function newPasswordError(password: string, confirm: string): string | null {
  return passwordRuleError(password) ?? (password !== confirm ? '새 비밀번호와 확인 값이 다릅니다.' : null)
}
