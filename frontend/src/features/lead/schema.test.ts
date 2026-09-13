import { describe, expect, it } from 'vitest'
import { leadFormDefaults, leadFormSchema } from './schema'

const valid = { ...leadFormDefaults, name: '홍길동', phone: '010-1234-5678', agreePrivacy: true }

describe('상담신청 입력 검증', () => {
  it('정상 입력은 통과한다', () => {
    expect(leadFormSchema.safeParse(valid).success).toBe(true)
    expect(leadFormSchema.safeParse({ ...valid, phone: '01112345678' }).success).toBe(true)
  })

  it.each(['02-123-4567', '010-12-5678', '01012345', '015-1234-5678'])('휴대폰이 아닌 번호 %s 는 거부한다', (phone) => {
    expect(leadFormSchema.safeParse({ ...valid, phone }).success).toBe(false)
  })

  it('이름 1자, 필수 동의 누락은 거부한다', () => {
    const result = leadFormSchema.safeParse({ ...valid, name: '홍', agreePrivacy: false })
    expect(result.success).toBe(false)
    const fields = result.error?.issues.map((issue) => issue.path[0])
    expect(fields).toEqual(expect.arrayContaining(['name', 'agreePrivacy']))
  })

  it('이름 앞뒤 공백은 제거한다', () => {
    const result = leadFormSchema.parse({ ...valid, name: '  김철수 ' })
    expect(result.name).toBe('김철수')
  })
})
