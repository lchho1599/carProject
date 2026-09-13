import { useQuery } from '@tanstack/react-query'
import { apiGet } from '../../lib/apiClient'

export type SiteInfo = {
  name: string
  phone: string
  businessName: string
  representative: string
  businessNumber: string
  address: string
}

export function useSite() {
  return useQuery({
    queryKey: ['site'],
    queryFn: () => apiGet<SiteInfo>('/api/site'),
    staleTime: Infinity,
  })
}
