import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, useParams } from 'react-router'
import { StatusBadge } from '../../components/admin/StatusBadge'
import { Button } from '../../components/ui/Button'
import { ErrorState } from '../../components/ui/StateViews'
import { Spinner } from '../../components/ui/Spinner'
import { useAddLeadNote, useAdminLead, useChangeLeadStatus, type AdminLeadDetail } from '../../features/admin/leads'
import { LEAD_STATUSES, leadStatusLabel, leadTypeLabel, type LeadStatus } from '../../features/lead/labels'
import { ApiError } from '../../lib/apiClient'
import { formatDateTime, formatWon } from '../../lib/format'

export function LeadDetailPage() {
  const leadId = Number(useParams().leadId)
  const { data: lead, isPending, error, refetch } = useAdminLead(leadId)

  if (isPending) {
    return (
      <div className="flex justify-center py-20 text-primary">
        <Spinner className="size-8" />
      </div>
    )
  }
  if (error || !lead) {
    const notFound = error instanceof ApiError && error.status === 404
    return (
      <div className="space-y-4">
        <BackLink />
        <ErrorState message={notFound ? '상담 신청을 찾을 수 없습니다.' : undefined} onRetry={notFound ? undefined : () => refetch()} />
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <BackLink />

      <section className="flex flex-wrap items-start justify-between gap-4 rounded-xl bg-white p-4 md:p-5">
        <div>
          <div className="flex items-center gap-2">
            <StatusBadge status={lead.status} />
            <span className="text-sm text-gray-500">
              {leadTypeLabel[lead.type]} · {formatDateTime(lead.createdAt)} · #{lead.id}
            </span>
          </div>
          <h1 className="mt-2 text-2xl font-extrabold">{lead.name}</h1>
          <a href={`tel:${lead.phone}`} className="mt-1 inline-block text-lg font-bold text-primary hover:underline">
            {lead.phone}
          </a>
        </div>
        {/* 저장 후 상태가 바뀌면 key 가 달라져 선택값이 새 상태로 초기화된다 */}
        <StatusChanger key={lead.status} lead={lead} />
      </section>

      <div className="grid gap-4 lg:grid-cols-[1fr_380px]">
        <div className="space-y-4">
          <Card title="신청 차량·조건">
            <InfoRow label="차량">{lead.vehicleName}</InfoRow>
            <VehicleSnapshotRows snapshot={lead.vehicleSnapshot} />
            <InfoRow label="차량가">{lead.totalPrice !== null ? formatWon(lead.totalPrice) : '-'}</InfoRow>
            <InfoRow label="이용조건">{lead.conditionSummary || '-'}</InfoRow>
          </Card>

          <Card title="동의·유입 정보">
            <InfoRow label="개인정보 동의">{lead.agreePrivacy ? '동의' : '미동의'}</InfoRow>
            <InfoRow label="마케팅 수신">{lead.agreeMarketing ? '동의' : '미동의'}</InfoRow>
            <InfoRow label="동의 일시">{formatDateTime(lead.agreedAt)}</InfoRow>
            <InfoRow label="신청 페이지">{lead.sourceUrl || '-'}</InfoRow>
            <InfoRow label="광고 유입">
              {lead.utm ? Object.entries(lead.utm).map(([key, value]) => `${key}=${value}`).join(', ') : '-'}
            </InfoRow>
            <InfoRow label="브라우저">
              <span className="text-xs break-all text-gray-500">{lead.userAgent || '-'}</span>
            </InfoRow>
          </Card>

          <Card title="알림 메일 발송">
            {lead.notifications.length === 0 ? (
              <p className="text-sm text-gray-500">발송 기록이 없습니다. (설정의 알림 수신 메일이 비어 있으면 발송하지 않습니다)</p>
            ) : (
              <ul className="space-y-1 text-sm">
                {lead.notifications.map((notification, index) => (
                  <li key={index} className="flex justify-between gap-2">
                    <span>{notification.recipient}</span>
                    <span className={notification.status === 'SENT' ? 'text-green-700' : 'text-red-600'}>
                      {notification.status === 'SENT' ? '발송' : '실패'} · {formatDateTime(notification.createdAt)}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>

        <NotesCard lead={lead} />
      </div>
    </div>
  )
}

function StatusChanger({ lead }: { lead: AdminLeadDetail }) {
  const [status, setStatus] = useState<LeadStatus>(lead.status)
  const changeStatus = useChangeLeadStatus(lead.id)

  return (
    <div className="flex items-end gap-2">
      <label className="text-xs font-semibold text-gray-500">
        상태 변경
        <select
          value={status}
          onChange={(event) => setStatus(event.target.value as LeadStatus)}
          className="mt-1 block h-10 rounded-lg border border-gray-300 bg-white px-3 text-sm"
        >
          {LEAD_STATUSES.map((value) => (
            <option key={value} value={value}>
              {leadStatusLabel[value]}
            </option>
          ))}
        </select>
      </label>
      <Button
        size="sm"
        className="h-10"
        disabled={status === lead.status}
        loading={changeStatus.isPending}
        onClick={() => changeStatus.mutate(status)}
      >
        저장
      </Button>
      {changeStatus.isError && (
        <p role="alert" className="text-sm text-red-600">
          {changeStatus.error instanceof ApiError ? changeStatus.error.message : '저장하지 못했습니다.'}
        </p>
      )}
    </div>
  )
}

function NotesCard({ lead }: { lead: AdminLeadDetail }) {
  const [content, setContent] = useState('')
  const addNote = useAddLeadNote(lead.id)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    if (!content.trim()) return
    addNote.mutate(content, { onSuccess: () => setContent('') })
  }

  return (
    <Card title={`상담 메모 (${lead.notes.length})`}>
      <form onSubmit={submit} className="space-y-2">
        <label htmlFor="lead-note" className="sr-only">
          메모 내용
        </label>
        <textarea
          id="lead-note"
          rows={3}
          maxLength={2000}
          value={content}
          onChange={(event) => setContent(event.target.value)}
          placeholder="통화 내용, 다음 연락 일정 등을 남겨 주세요."
          className="w-full rounded-lg border border-gray-300 p-3 text-sm focus:border-primary focus:outline-none"
        />
        {addNote.isError && (
          <p role="alert" className="text-sm text-red-600">
            {addNote.error instanceof ApiError ? addNote.error.message : '메모를 저장하지 못했습니다.'}
          </p>
        )}
        <Button type="submit" size="sm" fullWidth disabled={!content.trim()} loading={addNote.isPending}>
          메모 추가
        </Button>
      </form>

      <ul className="mt-4 space-y-3">
        {lead.notes.map((note) => (
          <li key={note.id} className="rounded-lg bg-gray-50 p-3">
            <p className="text-sm whitespace-pre-wrap">{note.content}</p>
            <p className="mt-1 text-xs text-gray-500">
              {note.adminName} · {formatDateTime(note.createdAt)}
            </p>
          </li>
        ))}
      </ul>
    </Card>
  )
}

/** 신청 시점에 저장된 차량 정보(스냅샷) 표시 */
function VehicleSnapshotRows({ snapshot }: { snapshot: Record<string, unknown> | null }) {
  if (!snapshot) return null
  const color = snapshot.color as { name?: string; extraPrice?: number } | undefined
  const options = snapshot.options as { name?: string; price?: number }[] | undefined
  const deal = snapshot.deal as { title?: string; monthlyPrice?: number; periodMonths?: number } | undefined
  const stock = snapshot.instantStock as { exteriorColor?: string; interiorColor?: string; monthlyPrice?: number; conditionText?: string } | undefined

  return (
    <>
      {typeof snapshot.trim === 'string' && (
        <InfoRow label="세부모델">
          {snapshot.trim}
          {typeof snapshot.trimPrice === 'number' && <span className="text-gray-500"> ({formatWon(snapshot.trimPrice)})</span>}
        </InfoRow>
      )}
      {color?.name && (
        <InfoRow label="색상">
          {color.name}
          {color.extraPrice ? <span className="text-gray-500"> (+{formatWon(color.extraPrice)})</span> : null}
        </InfoRow>
      )}
      {options && options.length > 0 && (
        <InfoRow label="옵션">{options.map((option) => option.name).join(', ')}</InfoRow>
      )}
      {deal?.title && (
        <InfoRow label="특가">
          {deal.title}
          {deal.monthlyPrice !== undefined && ` · 월 ${formatWon(deal.monthlyPrice)}`}
          {deal.periodMonths !== undefined && ` · ${deal.periodMonths}개월`}
        </InfoRow>
      )}
      {stock?.exteriorColor && (
        <InfoRow label="즉시출고">
          {stock.exteriorColor}
          {stock.interiorColor && ` / ${stock.interiorColor}`}
          {stock.monthlyPrice !== undefined && ` · 월 ${formatWon(stock.monthlyPrice)}`}
          {stock.conditionText && ` (${stock.conditionText})`}
        </InfoRow>
      )}
    </>
  )
}

function BackLink() {
  return (
    <Link to="/admin/leads" className="inline-block text-sm font-semibold text-gray-600 hover:text-primary">
      ‹ 목록으로
    </Link>
  )
}

function Card({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section aria-label={title} className="rounded-xl bg-white p-4 md:p-5">
      <h2 className="mb-3 font-bold">{title}</h2>
      {children}
    </section>
  )
}

function InfoRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex gap-3 border-b border-gray-50 py-2 text-sm last:border-0">
      <span className="w-24 shrink-0 text-gray-500">{label}</span>
      <span className="min-w-0 flex-1">{children}</span>
    </div>
  )
}
