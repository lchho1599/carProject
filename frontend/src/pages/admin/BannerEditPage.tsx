import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { AdminLoadError, AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { DisplayStatusBadge } from '../../components/admin/DisplayStatusBadge'
import { TextField, ToggleField } from '../../components/admin/form'
import { fieldErrorMap, formErrorMessage, toNumberOrNull } from '../../components/admin/formUtils'
import { ImageUploadField } from '../../components/admin/ImageUploadField'
import { Button } from '../../components/ui/Button'
import { useAdminBanner, useDeleteResource, useSaveResource, type AdminBanner, type BannerInput } from '../../features/admin/catalog'
import { fromDatetimeLocal, nowDatetimeLocal, toDatetimeLocal } from '../../lib/datetime'

/** 배너 등록(/admin/banners/new) · 수정(/admin/banners/:bannerId) */
export function BannerEditPage() {
  const { bannerId: idParam } = useParams()
  const isNew = idParam === 'new'
  const bannerId = isNew ? undefined : Number(idParam)
  const banner = useAdminBanner(bannerId)

  if (!isNew && banner.isPending) return <AdminLoading />
  if (!isNew && (banner.isError || !banner.data)) {
    return <AdminLoadError error={banner.error} onRetry={() => banner.refetch()} backTo="/admin/banners" />
  }
  return <BannerForm key={banner.data?.id ?? 'new'} banner={banner.data} />
}

function BannerForm({ banner }: { banner?: AdminBanner }) {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    title: banner?.title ?? '',
    imagePcUrl: banner?.imagePcUrl ?? (null as string | null),
    imageMobileUrl: banner?.imageMobileUrl ?? (null as string | null),
    linkUrl: banner?.linkUrl ?? '',
    startsAt: banner ? toDatetimeLocal(banner.startsAt) : nowDatetimeLocal(),
    endsAt: toDatetimeLocal(banner?.endsAt),
    sortOrder: String(banner?.sortOrder ?? 0),
    published: banner?.published ?? false,
  })
  const save = useSaveResource<BannerInput, AdminBanner>('banners')
  const remove = useDeleteResource('banners')
  const errors = fieldErrorMap(save.error)
  const message = formErrorMessage(save.error)
  const set = <K extends keyof typeof form>(key: K, value: (typeof form)[K]) => setForm((current) => ({ ...current, [key]: value }))

  return (
    <div className="space-y-4">
      <AdminPageHeader title={banner ? '배너 수정' : '배너 등록'} backTo="/admin/banners" actions={banner && <DisplayStatusBadge status={banner.displayStatus} />} />
      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault()
          const input: BannerInput = {
            title: form.title,
            imagePcUrl: form.imagePcUrl ?? '',
            imageMobileUrl: form.imageMobileUrl ?? '',
            linkUrl: form.linkUrl || null,
            startsAt: fromDatetimeLocal(form.startsAt) as string,
            endsAt: fromDatetimeLocal(form.endsAt),
            sortOrder: toNumberOrNull(form.sortOrder) ?? 0,
            published: form.published,
          }
          save.mutate({ id: banner?.id, input }, { onSuccess: () => navigate('/admin/banners') })
        }}
      >
        <section aria-label="배너 이미지" className="grid gap-4 rounded-xl bg-white p-4 md:grid-cols-[2fr_1fr] md:p-5">
          <ImageUploadField label="PC 이미지" required value={form.imagePcUrl} error={errors.imagePcUrl} hint="권장 크기 1800×600 (가로:세로 3:1)" previewClassName="aspect-[3/1]" onChange={(url) => set('imagePcUrl', url)} />
          <ImageUploadField label="모바일 이미지" required value={form.imageMobileUrl} error={errors.imageMobileUrl} hint="권장 크기 1080×608 (16:9)" previewClassName="aspect-[16/9]" onChange={(url) => set('imageMobileUrl', url)} />
        </section>

        <section aria-label="배너 정보" className="grid gap-3 rounded-xl bg-white p-4 sm:grid-cols-2 md:p-5 lg:grid-cols-4">
          <TextField label="배너 제목" required className="sm:col-span-2" hint="이미지 대체 문구로도 쓰입니다." value={form.title} error={errors.title} onChange={(e) => set('title', e.target.value)} />
          <TextField label="링크" className="sm:col-span-2" placeholder="/deals?type=time" hint="/ 로 시작하는 사이트 경로 또는 https:// 주소" value={form.linkUrl} error={errors.linkUrl} onChange={(e) => set('linkUrl', e.target.value)} />
          <TextField label="노출 시작" required type="datetime-local" value={form.startsAt} error={errors.startsAt} onChange={(e) => set('startsAt', e.target.value)} />
          <TextField label="노출 종료" type="datetime-local" hint="비우면 계속 노출됩니다." value={form.endsAt} error={errors.endsAt} onChange={(e) => set('endsAt', e.target.value)} />
          <TextField label="정렬 순서" type="number" min={0} value={form.sortOrder} onChange={(e) => set('sortOrder', e.target.value)} />
          <ToggleField label="공개" checked={form.published} onChange={(published) => set('published', published)} />
        </section>

        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>}
        <div className="flex flex-wrap justify-between gap-2">
          <Button type="submit" loading={save.isPending}>
            {banner ? '수정 저장' : '배너 등록'}
          </Button>
          {banner && (
            <Button
              variant="outline"
              className="text-red-600"
              loading={remove.isPending}
              onClick={() => {
                if (window.confirm('이 배너를 삭제할까요?')) remove.mutate(banner.id, { onSuccess: () => navigate('/admin/banners') })
              }}
            >
              삭제
            </Button>
          )}
        </div>
      </form>
    </div>
  )
}
