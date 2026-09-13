import { MutationCache, QueryCache, QueryClient } from '@tanstack/react-query'
import { ApiError } from './apiClient'

/**
 * 관리자 화면에서 세션이 만료(401)되면 로그인 화면으로 보낸다.
 * 로그인 확인 자체(/me)는 AdminLayout 이 처리하므로 여기서는 그 밖의 요청만 다룬다.
 */
function handleAdminSessionExpired(error: unknown, queryKey?: readonly unknown[]) {
  if (!(error instanceof ApiError) || error.status !== 401) return
  const { pathname, search } = window.location
  if (!pathname.startsWith('/admin') || pathname.startsWith('/admin/login')) return
  if (queryKey?.[0] === 'admin' && queryKey?.[1] === 'me') return
  const redirect = encodeURIComponent(pathname + search)
  window.location.assign(`/admin/login?expired=1&redirect=${redirect}`)
}

export const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error, query) => handleAdminSessionExpired(error, query.queryKey),
  }),
  mutationCache: new MutationCache({
    onError: (error) => handleAdminSessionExpired(error),
  }),
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      refetchOnWindowFocus: false,
      // 4xx(없는 데이터, 잘못된 요청, 로그인 필요)는 재시도해도 결과가 같으므로 재시도하지 않는다
      retry: (failureCount, error) =>
        !(error instanceof ApiError && error.status >= 400 && error.status < 500) && failureCount < 2,
    },
  },
})
