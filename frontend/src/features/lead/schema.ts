import { z } from 'zod'

/** 백엔드 검증 규칙과 같은 휴대폰 형식 (하이픈 선택) */
export const PHONE_PATTERN = /^01[016789]-?\d{3,4}-?\d{4}$/

export const leadFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(2, '이름을 2자 이상 입력해 주세요.')
    .max(20, '이름은 20자 이내로 입력해 주세요.'),
  phone: z
    .string()
    .trim()
    .min(1, '연락처를 입력해 주세요.')
    .regex(PHONE_PATTERN, '휴대폰 번호 형식이 올바르지 않습니다.'),
  agreePrivacy: z.boolean().refine((agreed) => agreed, '개인정보 수집·이용에 동의해 주세요.'),
  agreeMarketing: z.boolean(),
  website: z.string().optional(),
})

export type LeadFormValues = z.infer<typeof leadFormSchema>

export const leadFormDefaults: LeadFormValues = {
  name: '',
  phone: '',
  agreePrivacy: false,
  agreeMarketing: false,
  website: '',
}
