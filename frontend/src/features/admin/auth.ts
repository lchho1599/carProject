import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost, setCsrfToken } from '../../lib/apiClient'

export type AdminMe = {
  id: number
  email: string
  name: string
  csrfToken: string
}

export const adminMeKey = ['admin', 'me'] as const

/** 현재 로그인한 관리자 — 로그인하지 않았으면 401 오류 */
export function useAdminMe() {
  return useQuery({
    queryKey: adminMeKey,
    queryFn: async () => {
      const me = await apiGet<AdminMe>('/api/admin/auth/me')
      setCsrfToken(me.csrfToken)
      return me
    },
    retry: false,
    staleTime: 5 * 60_000,
  })
}

export function useAdminLogin() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (credentials: { email: string; password: string }) =>
      apiPost<AdminMe>('/api/admin/auth/login', credentials),
    onSuccess: (me) => {
      setCsrfToken(me.csrfToken)
      queryClient.setQueryData(adminMeKey, me)
    },
  })
}

export function useAdminLogout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => apiPost<{ result: string }>('/api/admin/auth/logout'),
    onSettled: () => {
      setCsrfToken(null)
      queryClient.removeQueries({ queryKey: ['admin'] })
    },
  })
}
