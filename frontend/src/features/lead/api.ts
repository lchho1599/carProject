import { useMutation } from '@tanstack/react-query'
import { apiPost } from '../../lib/apiClient'
import type { LeadCreateRequest, LeadCreateResponse } from './types'

export function createLead(request: LeadCreateRequest): Promise<LeadCreateResponse> {
  return apiPost<LeadCreateResponse>('/api/leads', request)
}

export function useCreateLead() {
  return useMutation({ mutationFn: createLead })
}
