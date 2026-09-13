import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost, apiPut } from '../../lib/apiClient'
import { adminMeKey } from './auth'

export type AdminAccount = {
  id: number
  email: string
  name: string
  active: boolean
  lastLoginAt: string | null
  createdAt: string
  /** 로그인한 본인 계정 */
  me: boolean
}

const accountsKey = ['admin', 'accounts'] as const

export function useAdminAccounts() {
  return useQuery({ queryKey: accountsKey, queryFn: () => apiGet<AdminAccount[]>('/api/admin/users') })
}

function useRefreshAccounts() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: accountsKey })
    // 본인 이름을 바꿨을 수 있으므로 상단 표시도 새로 불러온다
    queryClient.invalidateQueries({ queryKey: adminMeKey })
  }
}

export function useCreateAdmin() {
  const refresh = useRefreshAccounts()
  return useMutation({
    mutationFn: (input: { email: string; name: string; password: string }) => apiPost<AdminAccount>('/api/admin/users', input),
    onSuccess: refresh,
  })
}

export function useUpdateAdmin() {
  const refresh = useRefreshAccounts()
  return useMutation({
    mutationFn: ({ id, name, active }: { id: number; name: string; active: boolean }) =>
      apiPut<AdminAccount>(`/api/admin/users/${id}`, { name, active }),
    onSuccess: refresh,
  })
}

export function useResetAdminPassword() {
  return useMutation({
    mutationFn: ({ id, newPassword }: { id: number; newPassword: string }) =>
      apiPut<void>(`/api/admin/users/${id}/password`, { newPassword }),
  })
}

export function useChangeMyPassword() {
  return useMutation({
    mutationFn: (input: { currentPassword: string; newPassword: string }) => apiPut<void>('/api/admin/users/me/password', input),
  })
}
