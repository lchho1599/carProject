import { useQuery } from '@tanstack/react-query'
import { apiGet } from '../../lib/apiClient'

export type Banner = {
  id: number
  title: string
  imagePcUrl: string
  imageMobileUrl: string
  linkUrl: string | null
}

export function useBanners() {
  return useQuery({
    queryKey: ['banners'],
    queryFn: () => apiGet<Banner[]>('/api/banners'),
    staleTime: 5 * 60_000,
  })
}
