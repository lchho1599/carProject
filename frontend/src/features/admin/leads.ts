import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPatch, apiPost, apiUrl } from '../../lib/apiClient'
import type { LeadStatus } from '../lead/labels'
import type { LeadType } from '../lead/types'

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminLeadListItem = {
  id: number
  type: LeadType
  status: LeadStatus
  maskedName: string
  maskedPhone: string
  vehicleName: string
  totalPrice: number | null
  createdAt: string
}

export type AdminLeadNote = { id: number; content: string; adminName: string; createdAt: string }

export type AdminLeadDetail = {
  id: number
  type: LeadType
  status: LeadStatus
  name: string
  phone: string
  vehicleName: string
  vehicleSnapshot: Record<string, unknown> | null
  conditions: Record<string, unknown> | null
  conditionSummary: string
  totalPrice: number | null
  dealId: number | null
  instantStockId: number | null
  agreePrivacy: boolean
  agreeMarketing: boolean
  agreedAt: string
  sourceUrl: string | null
  utm: Record<string, string> | null
  userAgent: string | null
  createdAt: string
  updatedAt: string
  notes: AdminLeadNote[]
  notifications: { recipient: string; status: 'SENT' | 'FAILED'; createdAt: string }[]
}

export type Dashboard = {
  todayCount: number
  weekCount: number
  monthCount: number
  totalCount: number
  statusCounts: Record<LeadStatus, number>
  recentLeads: AdminLeadListItem[]
}

/** 목록 필터 — 주소창 파라미터와 같은 이름 */
export type LeadFilters = {
  from?: string
  to?: string
  type?: LeadType
  status?: LeadStatus
  q?: string
  page?: number
}

export function leadFilterQuery(filters: LeadFilters, { withPage = true } = {}): string {
  const params = new URLSearchParams()
  if (filters.from) params.set('from', filters.from)
  if (filters.to) params.set('to', filters.to)
  if (filters.type) params.set('type', filters.type)
  if (filters.status) params.set('status', filters.status)
  if (filters.q?.trim()) params.set('q', filters.q.trim())
  if (withPage && filters.page) params.set('page', String(filters.page))
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function leadExportUrl(filters: LeadFilters): string {
  return apiUrl(`/api/admin/leads/export.csv${leadFilterQuery(filters, { withPage: false })}`)
}

export function useDashboard() {
  return useQuery({
    queryKey: ['admin', 'dashboard'],
    queryFn: () => apiGet<Dashboard>('/api/admin/dashboard'),
    staleTime: 30_000,
  })
}

export function useAdminLeads(filters: LeadFilters) {
  return useQuery({
    queryKey: ['admin', 'leads', filters],
    queryFn: () => apiGet<PageResponse<AdminLeadListItem>>(`/api/admin/leads${leadFilterQuery(filters)}`),
    placeholderData: keepPreviousData,
    staleTime: 10_000,
  })
}

export function useAdminLead(leadId: number) {
  return useQuery({
    queryKey: ['admin', 'lead', leadId],
    queryFn: () => apiGet<AdminLeadDetail>(`/api/admin/leads/${leadId}`),
    enabled: Number.isFinite(leadId),
    staleTime: 0,
  })
}

function useInvalidateLeads(leadId: number) {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: ['admin', 'lead', leadId] })
    queryClient.invalidateQueries({ queryKey: ['admin', 'leads'] })
    queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] })
  }
}

export function useChangeLeadStatus(leadId: number) {
  const invalidate = useInvalidateLeads(leadId)
  return useMutation({
    mutationFn: (status: LeadStatus) => apiPatch<void>(`/api/admin/leads/${leadId}`, { status }),
    onSuccess: invalidate,
  })
}

export function useAddLeadNote(leadId: number) {
  const invalidate = useInvalidateLeads(leadId)
  return useMutation({
    mutationFn: (content: string) => apiPost<AdminLeadNote>(`/api/admin/leads/${leadId}/notes`, { content }),
    onSuccess: invalidate,
  })
}
