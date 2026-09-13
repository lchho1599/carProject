import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut, apiUpload } from '../../lib/apiClient'
import type { DealType } from '../deal/api'
import type { StockStatus } from '../instant/api'
import type { BodyType, FuelType, Origin, VehicleSummary } from '../vehicle/types'

// ================================================================ 타입 (백엔드 Admin*Dtos 와 같은 형식)

export type DisplayStatus = 'VISIBLE' | 'SCHEDULED' | 'ENDED' | 'HIDDEN'

export type AdminBrand = { id: number; name: string; origin: Origin; logoUrl: string | null; sortOrder: number; active: boolean }
export type BrandInput = Omit<AdminBrand, 'id'>

export type AdminModel = {
  id: number
  brandId: number
  brandName: string
  name: string
  segment: string | null
  bodyType: BodyType
  fuel: FuelType
  imageUrl: string | null
  sortOrder: number
  active: boolean
}
export type ModelInput = Omit<AdminModel, 'id' | 'brandName'>

export type AdminTrim = { id: number; name: string; price: number; sortOrder: number; active: boolean }
export type AdminOption = AdminTrim
export type AdminColor = { id: number; name: string; hexCode: string | null; extraPrice: number; sortOrder: number; active: boolean }
export type AdminModelDetail = { model: AdminModel; trims: AdminTrim[]; options: AdminOption[]; colors: AdminColor[] }

export type AdminDeal = {
  id: number
  type: DealType
  title: string
  badge: string | null
  originalMonthly: number | null
  monthlyPrice: number
  leaseMonthly: number | null
  periodMonths: number
  depositRate: number
  prepayRate: number
  startsAt: string
  endsAt: string | null
  sortOrder: number
  published: boolean
  displayStatus: DisplayStatus
  vehicleActive: boolean
  vehicle: VehicleSummary
}
export type DealInput = Omit<AdminDeal, 'id' | 'displayStatus' | 'vehicleActive' | 'vehicle'> & { trimId: number | null }

export type AdminInstant = {
  id: number
  exteriorColor: string
  interiorColor: string | null
  optionsText: string | null
  vehiclePrice: number
  monthlyPrice: number
  conditionText: string
  badge: string | null
  status: StockStatus
  sortOrder: number
  published: boolean
  visible: boolean
  vehicleActive: boolean
  updatedAt: string
  vehicle: VehicleSummary
}
export type InstantInput = Omit<AdminInstant, 'id' | 'visible' | 'vehicleActive' | 'updatedAt' | 'vehicle'> & {
  trimId: number | null
}

export type AdminBanner = {
  id: number
  title: string
  imagePcUrl: string
  imageMobileUrl: string
  linkUrl: string | null
  startsAt: string
  endsAt: string | null
  sortOrder: number
  published: boolean
  displayStatus: DisplayStatus
}
export type BannerInput = Omit<AdminBanner, 'id' | 'displayStatus'>

// ================================================================ 차량

export function useAdminBrands() {
  return useQuery({ queryKey: ['admin', 'brands'], queryFn: () => apiGet<AdminBrand[]>('/api/admin/brands') })
}

export function useAdminModels(brandId: number | undefined) {
  return useQuery({
    queryKey: ['admin', 'models', brandId],
    queryFn: () => apiGet<AdminModel[]>(`/api/admin/brands/${brandId}/models`),
    enabled: brandId !== undefined,
  })
}

export function useAdminModel(modelId: number | undefined) {
  return useQuery({
    queryKey: ['admin', 'model', modelId],
    queryFn: () => apiGet<AdminModelDetail>(`/api/admin/models/${modelId}`),
    enabled: modelId !== undefined,
  })
}

/** 저장 후 관리자·사용자 화면의 관련 조회를 모두 새로 불러오게 한다 */
function useInvalidateCatalog() {
  const queryClient = useQueryClient()
  return () => {
    for (const key of [['admin'], ['brands'], ['models'], ['model'], ['deals'], ['instant'], ['banners']]) {
      queryClient.invalidateQueries({ queryKey: key })
    }
  }
}

export function useSaveBrand() {
  const invalidate = useInvalidateCatalog()
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: BrandInput }) =>
      id ? apiPut<AdminBrand>(`/api/admin/brands/${id}`, input) : apiPost<AdminBrand>('/api/admin/brands', input),
    onSuccess: invalidate,
  })
}

export function useSaveModel() {
  const invalidate = useInvalidateCatalog()
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: ModelInput }) =>
      id ? apiPut<AdminModel>(`/api/admin/models/${id}`, input) : apiPost<AdminModel>('/api/admin/models', input),
    onSuccess: invalidate,
  })
}

export type ModelItemKind = 'trims' | 'options' | 'colors'

/** 트림·옵션·색상 등록(POST /models/{id}/{kind}) / 수정(PUT /{kind}/{itemId}) */
export function useSaveModelItem(modelId: number, kind: ModelItemKind) {
  const invalidate = useInvalidateCatalog()
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: Record<string, unknown> }) =>
      id ? apiPut(`/api/admin/${kind}/${id}`, input) : apiPost(`/api/admin/models/${modelId}/${kind}`, input),
    onSuccess: invalidate,
  })
}

// ================================================================ 특가·즉시출고·배너

export function useAdminDeals(type?: DealType) {
  return useQuery({
    queryKey: ['admin', 'deals', type ?? 'ALL'],
    queryFn: () => apiGet<AdminDeal[]>(type ? `/api/admin/deals?type=${type}` : '/api/admin/deals'),
  })
}

export function useAdminDeal(id: number | undefined) {
  return useQuery({
    queryKey: ['admin', 'deal', id],
    queryFn: () => apiGet<AdminDeal>(`/api/admin/deals/${id}`),
    enabled: id !== undefined,
  })
}

export function useAdminInstants(status?: StockStatus) {
  return useQuery({
    queryKey: ['admin', 'instants', status ?? 'ALL'],
    queryFn: () => apiGet<AdminInstant[]>(status ? `/api/admin/instant?status=${status}` : '/api/admin/instant'),
  })
}

export function useAdminInstant(id: number | undefined) {
  return useQuery({
    queryKey: ['admin', 'instant', id],
    queryFn: () => apiGet<AdminInstant>(`/api/admin/instant/${id}`),
    enabled: id !== undefined,
  })
}

export function useAdminBanners() {
  return useQuery({ queryKey: ['admin', 'banners'], queryFn: () => apiGet<AdminBanner[]>('/api/admin/banners') })
}

export function useAdminBanner(id: number | undefined) {
  return useQuery({
    queryKey: ['admin', 'banner', id],
    queryFn: () => apiGet<AdminBanner>(`/api/admin/banners/${id}`),
    enabled: id !== undefined,
  })
}

/** 특가·즉시출고·배너 공통 저장/삭제 — resource: deals | instant | banners */
export function useSaveResource<TInput, TResult extends { id: number }>(resource: 'deals' | 'instant' | 'banners') {
  const invalidate = useInvalidateCatalog()
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: TInput }) =>
      id ? apiPut<TResult>(`/api/admin/${resource}/${id}`, input) : apiPost<TResult>(`/api/admin/${resource}`, input),
    onSuccess: invalidate,
  })
}

export function useDeleteResource(resource: 'deals' | 'instant' | 'banners') {
  const invalidate = useInvalidateCatalog()
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/api/admin/${resource}/${id}`),
    onSuccess: invalidate,
  })
}

export function useUploadImage() {
  return useMutation({ mutationFn: (file: File) => apiUpload<{ url: string }>('/api/admin/uploads', file) })
}
