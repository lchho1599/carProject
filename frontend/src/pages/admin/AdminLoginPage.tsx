import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Navigate, useNavigate, useSearchParams } from 'react-router'
import { z } from 'zod'
import { Button } from '../../components/ui/Button'
import { Spinner } from '../../components/ui/Spinner'
import { site } from '../../config/site'
import { useAdminLogin, useAdminMe } from '../../features/admin/auth'
import { ApiError } from '../../lib/apiClient'
import { cn } from '../../lib/cn'
import { useNoIndex } from '../../lib/useNoIndex'

const loginSchema = z.object({
  email: z.string().trim().min(1, '이메일을 입력해 주세요.').email('이메일 형식이 올바르지 않습니다.'),
  password: z.string().min(1, '비밀번호를 입력해 주세요.'),
})
type LoginValues = z.infer<typeof loginSchema>

/** 로그인 후 돌아갈 주소 — 관리자 경로만 허용 (외부 주소로 보내는 공격 방지) */
function safeRedirect(value: string | null): string {
  if (!value) return '/admin'
  return value.startsWith('/admin') && !value.startsWith('//') ? value : '/admin'
}

export function AdminLoginPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const redirectTo = safeRedirect(searchParams.get('redirect'))
  const expired = searchParams.get('expired') === '1'

  useNoIndex()
  const me = useAdminMe()
  const login = useAdminLogin()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginValues>({ resolver: zodResolver(loginSchema), defaultValues: { email: '', password: '' } })

  if (me.isPending) {
    return (
      <div className="flex min-h-dvh items-center justify-center text-primary">
        <Spinner className="size-8" />
      </div>
    )
  }
  if (me.data) {
    return <Navigate to={redirectTo} replace />
  }

  const onSubmit = handleSubmit((values) =>
    login.mutate(values, { onSuccess: () => navigate(redirectTo, { replace: true }) }),
  )

  const serverMessage =
    login.error instanceof ApiError ? login.error.message : login.error ? '로그인 중 오류가 발생했습니다.' : null

  return (
    <div className="flex min-h-dvh items-center justify-center bg-gray-100 p-4">
      <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-sm md:p-8">
        <h1 className="text-center text-xl font-extrabold text-primary">{site.name} 관리자</h1>
        <p className="mt-1 text-center text-sm text-gray-500">관리자 계정으로 로그인하세요.</p>

        {expired && !serverMessage && (
          <p role="status" className="mt-5 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
            로그인이 만료되었습니다. 다시 로그인해 주세요.
          </p>
        )}

        <form onSubmit={onSubmit} noValidate className="mt-6 space-y-4">
          <div>
            <label htmlFor="admin-email" className="mb-1 block text-sm font-semibold text-gray-700">
              이메일
            </label>
            <input
              id="admin-email"
              type="email"
              autoComplete="username"
              aria-invalid={!!errors.email}
              className={inputClass(!!errors.email)}
              {...register('email')}
            />
            {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
          </div>
          <div>
            <label htmlFor="admin-password" className="mb-1 block text-sm font-semibold text-gray-700">
              비밀번호
            </label>
            <input
              id="admin-password"
              type="password"
              autoComplete="current-password"
              aria-invalid={!!errors.password}
              className={inputClass(!!errors.password)}
              {...register('password')}
            />
            {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>}
          </div>

          {serverMessage && (
            <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {serverMessage}
            </p>
          )}

          <Button type="submit" fullWidth size="lg" loading={login.isPending}>
            로그인
          </Button>
        </form>
      </div>
    </div>
  )
}

function inputClass(invalid: boolean) {
  return cn(
    'h-11 w-full rounded-lg border px-3 focus:border-primary focus:outline-none',
    invalid ? 'border-red-400' : 'border-gray-300',
  )
}
