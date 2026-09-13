import { useQuery } from '@tanstack/react-query'
import { apiGet } from '../../lib/apiClient'
import type { Brand, ModelDetail, ModelSummary, Origin } from './types'

export function useBrands(origin?: Origin) {
  return useQuery({
    queryKey: ['brands', origin ?? 'ALL'],
    queryFn: () => apiGet<Brand[]>(origin ? `/api/brands?origin=${origin}` : '/api/brands'),
    staleTime: 5 * 60_000,
  })
}

export function useModels(brandId: number | undefined) {
  return useQuery({
    queryKey: ['models', brandId],
    queryFn: () => apiGet<ModelSummary[]>(`/api/brands/${brandId}/models`),
    enabled: brandId !== undefined,
    staleTime: 5 * 60_000,
  })
}

export function useModel(modelId: number | undefined) {
  return useQuery({
    queryKey: ['model', modelId],
    queryFn: () => apiGet<ModelDetail>(`/api/models/${modelId}`),
    enabled: modelId !== undefined,
    staleTime: 5 * 60_000,
  })
}
