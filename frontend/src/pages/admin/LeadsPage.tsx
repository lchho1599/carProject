import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { Pagination } from '../../components/admin/Pagination'
import { StatusBadge } from '../../components/admin/StatusBadge'
import { Button } from '../../components/ui/Button'
import { ErrorState } from '../../components/ui/StateViews'
import { Spinner } from '../../components/ui/Spinner'
import { leadExportUrl, useAdminLeads, type LeadFilters } from '../../features/admin/leads'
import { LEAD_STATUSES, LEAD_TYPES, leadStatusLabel, leadTypeLabel, type LeadStatus } from '../../features/lead/labels'
import type { LeadType } from '../../features/lead/types'
import { formatDateTime, formatManwon } from '../../lib/format'

const fieldClass = 'h-10 w-full rounded-lg border border-gray-300 bg-white px-3 text-sm focus:border-primary focus:outline-none'

/** 주소창 파라미터 ↔ 필터 (새로고침·뒤로가기·링크 공유 시 유지) */
function filtersFromParams(params: URLSearchParams): LeadFilters {
  const type = params.get('type')
  const status = params.get('status')
  return {
    from: params.get('from') ?? undefined,
    to: params.get('to') ?? undefined,
    type: LEAD_TYPES.includes(type as LeadType) ? (type as LeadType) : undefined,
    status: LEAD_STATUSES.includes(status as LeadStatus) ? (status as LeadStatus) : undefined,
    q: params.get('q') ?? undefined,
    page: Math.max(0, Number(params.get('page') ?? 0) || 0),
  }
}

function paramsFromFilters(filters: LeadFilters): URLSearchParams {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== '' && !(key === 'page' && value === 0)) params.set(key, String(value))
  })
  return params
}

export function LeadsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const filters = filtersFromParams(searchParams)
  const { data, isPending, isError, isFetching, refetch } = useAdminLeads(filters)

  const goPage = (page: number) => setSearchParams(paramsFromFilters({ ...filters, page }))

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-extrabold">상담 신청</h1>
        <a
          href={leadExportUrl(filters)}
          className="inline-flex h-9 items-center rounded-lg border border-gray-300 bg-white px-3 text-sm font-semibold text-gray-800 hover:bg-gray-50"
        >
          CSV 다운로드
        </a>
      </div>

      {/* 주소가 바뀌면(뒤로가기, 대시보드 링크) key 가 달라져 입력칸이 주소 값으로 다시 채워진다 */}
      <LeadFilterForm
        key={searchParams.toString()}
        initial={filters}
        onApply={(next) => setSearchParams(paramsFromFilters({ ...next, page: 0 }))}
        onReset={() => setSearchParams(new URLSearchParams())}
      />

      {isPending ? (
        <div className="flex justify-center py-16 text-primary">
          <Spinner className="size-8" />
        </div>
      ) : isError ? (
        <ErrorState onRetry={() => refetch()} />
      ) : (
        <section aria-label="신청 목록" className={isFetching ? 'opacity-60 transition-opacity' : ''}>
          <p className="mb-2 text-sm text-gray-600">
            총 <strong>{data.totalElements.toLocaleString('ko-KR')}</strong>건
          </p>

          {data.content.length === 0 ? (
            <div className="rounded-xl bg-white py-12 text-center text-gray-500">조건에 맞는 신청이 없습니다.</div>
          ) : (
            <>
              {/* PC: 표 */}
              <div className="hidden overflow-x-auto rounded-xl bg-white md:block">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-left text-gray-500">
                    <tr>
                      <th className="px-4 py-3 font-semibold">신청일시</th>
                      <th className="px-4 py-3 font-semibold">유형</th>
                      <th className="px-4 py-3 font-semibold">차량</th>
                      <th className="px-4 py-3 font-semibold">이름</th>
                      <th className="px-4 py-3 font-semibold">연락처</th>
                      <th className="px-4 py-3 text-right font-semibold">차량가</th>
                      <th className="px-4 py-3 font-semibold">상태</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {data.content.map((lead) => (
                      <tr
                        key={lead.id}
                        onClick={() => navigate(`/admin/leads/${lead.id}`)}
                        className="cursor-pointer hover:bg-primary-50/40"
                      >
                        <td className="px-4 py-3 whitespace-nowrap text-gray-600">{formatDateTime(lead.createdAt)}</td>
                        <td className="px-4 py-3 whitespace-nowrap">{leadTypeLabel[lead.type]}</td>
                        <td className="px-4 py-3">
                          <Link to={`/admin/leads/${lead.id}`} className="font-semibold hover:underline" onClick={(e) => e.stopPropagation()}>
                            {lead.vehicleName}
                          </Link>
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">{lead.maskedName}</td>
                        <td className="px-4 py-3 whitespace-nowrap">{lead.maskedPhone}</td>
                        <td className="px-4 py-3 text-right whitespace-nowrap">
                          {lead.totalPrice !== null ? formatManwon(lead.totalPrice) : '-'}
                        </td>
                        <td className="px-4 py-3">
                          <StatusBadge status={lead.status} />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* 모바일: 카드 */}
              <ul className="space-y-2 md:hidden">
                {data.content.map((lead) => (
                  <li key={lead.id}>
                    <Link to={`/admin/leads/${lead.id}`} className="block rounded-xl bg-white p-4">
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-sm text-gray-500">
                          {leadTypeLabel[lead.type]} · {formatDateTime(lead.createdAt)}
                        </span>
                        <StatusBadge status={lead.status} />
                      </div>
                      <p className="mt-1 font-bold">{lead.vehicleName}</p>
                      <p className="text-sm text-gray-600">
                        {lead.maskedName} · {lead.maskedPhone}
                      </p>
                    </Link>
                  </li>
                ))}
              </ul>

              <div className="mt-4">
                <Pagination page={data.page} totalPages={data.totalPages} onChange={goPage} />
              </div>
            </>
          )}
        </section>
      )}
    </div>
  )
}

function LeadFilterForm({
  initial,
  onApply,
  onReset,
}: {
  initial: LeadFilters
  onApply: (filters: LeadFilters) => void
  onReset: () => void
}) {
  const [draft, setDraft] = useState<LeadFilters>(initial)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onApply(draft)
  }

  return (
    <form onSubmit={submit} aria-label="검색 조건" className="grid gap-2 rounded-xl bg-white p-4 sm:grid-cols-2 lg:grid-cols-6">
      <label className="text-xs font-semibold text-gray-500">
        시작일
        <input type="date" className={fieldClass} value={draft.from ?? ''} onChange={(e) => setDraft({ ...draft, from: e.target.value || undefined })} />
      </label>
      <label className="text-xs font-semibold text-gray-500">
        종료일
        <input type="date" className={fieldClass} value={draft.to ?? ''} onChange={(e) => setDraft({ ...draft, to: e.target.value || undefined })} />
      </label>
      <label className="text-xs font-semibold text-gray-500">
        유형
        <select
          className={fieldClass}
          value={draft.type ?? ''}
          onChange={(e) => setDraft({ ...draft, type: (e.target.value || undefined) as LeadType | undefined })}
        >
          <option value="">전체</option>
          {LEAD_TYPES.map((type) => (
            <option key={type} value={type}>
              {leadTypeLabel[type]}
            </option>
          ))}
        </select>
      </label>
      <label className="text-xs font-semibold text-gray-500">
        상태
        <select
          className={fieldClass}
          value={draft.status ?? ''}
          onChange={(e) => setDraft({ ...draft, status: (e.target.value || undefined) as LeadStatus | undefined })}
        >
          <option value="">전체</option>
          {LEAD_STATUSES.map((status) => (
            <option key={status} value={status}>
              {leadStatusLabel[status]}
            </option>
          ))}
        </select>
      </label>
      <label className="text-xs font-semibold text-gray-500">
        검색
        <input
          type="search"
          placeholder="이름 또는 연락처 일부"
          className={fieldClass}
          value={draft.q ?? ''}
          onChange={(e) => setDraft({ ...draft, q: e.target.value })}
        />
      </label>
      <div className="flex items-end gap-2">
        <Button type="submit" size="sm" className="h-10 flex-1">
          검색
        </Button>
        <Button type="button" variant="outline" size="sm" className="h-10" onClick={onReset}>
          초기화
        </Button>
      </div>
    </form>
  )
}
