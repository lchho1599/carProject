import { useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import { AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { ActiveBadge } from '../../components/admin/DisplayStatusBadge'
import { SelectField, TextField, ToggleField } from '../../components/admin/form'
import { fieldErrorMap, formErrorMessage } from '../../components/admin/formUtils'
import { ImageUploadField } from '../../components/admin/ImageUploadField'
import { Button } from '../../components/ui/Button'
import { Modal } from '../../components/ui/Modal'
import { ErrorState } from '../../components/ui/StateViews'
import { useAdminBrands, useAdminModels, useSaveBrand, type AdminBrand, type BrandInput } from '../../features/admin/catalog'
import { bodyTypeLabel, fuelLabel } from '../../features/vehicle/labels'
import { cn } from '../../lib/cn'

/** 관리자 차량 관리 — 브랜드 목록 + 선택한 브랜드의 모델 목록 (?brand=) */
export function VehiclesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const brands = useAdminBrands()
  const selectedBrandId = Number(searchParams.get('brand')) || brands.data?.[0]?.id
  const selectedBrand = brands.data?.find((brand) => brand.id === selectedBrandId)
  const models = useAdminModels(selectedBrand?.id)
  const [editingBrand, setEditingBrand] = useState<AdminBrand | 'new' | null>(null)

  if (brands.isPending) return <AdminLoading />
  if (brands.isError) return <ErrorState onRetry={() => brands.refetch()} />

  return (
    <div className="space-y-4">
      <AdminPageHeader
        title="차량 관리"
        actions={
          <Button size="sm" onClick={() => setEditingBrand('new')}>
            브랜드 추가
          </Button>
        }
      />
      <p className="text-sm text-gray-500">
        차량 정보는 삭제하지 않고 <strong>사용 안 함</strong>으로 숨깁니다. 기존 상담 신청·특가가 참조하고 있기 때문입니다.
      </p>

      <div className="grid gap-4 lg:grid-cols-[280px_1fr]">
        <section aria-label="브랜드" className="rounded-xl bg-white p-3">
          <ul className="space-y-1">
            {brands.data.map((brand) => (
              <li key={brand.id} className="flex items-center gap-1">
                <button
                  type="button"
                  aria-pressed={brand.id === selectedBrand?.id}
                  onClick={() => setSearchParams({ brand: String(brand.id) }, { replace: true })}
                  className={cn(
                    'flex flex-1 items-center justify-between gap-2 rounded-lg px-3 py-2.5 text-left text-sm font-semibold',
                    brand.id === selectedBrand?.id ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100',
                  )}
                >
                  <span>
                    {brand.name}
                    <span className={cn('ml-1 text-xs', brand.id === selectedBrand?.id ? 'text-primary-100' : 'text-gray-400')}>
                      {brand.origin === 'IMPORTED' ? '수입' : '국산'}
                    </span>
                  </span>
                  {!brand.active && <span className="text-xs opacity-80">사용 안 함</span>}
                </button>
                <Button size="sm" variant="ghost" aria-label={`${brand.name} 수정`} onClick={() => setEditingBrand(brand)}>
                  수정
                </Button>
              </li>
            ))}
          </ul>
        </section>

        <section aria-label="모델" className="rounded-xl bg-white p-4">
          {!selectedBrand ? (
            <p className="py-10 text-center text-gray-500">브랜드를 추가해 주세요.</p>
          ) : (
            <>
              <div className="mb-3 flex items-center justify-between gap-2">
                <h2 className="text-lg font-extrabold">{selectedBrand.name} 모델</h2>
                <Link
                  to={`/admin/vehicles/models/new?brand=${selectedBrand.id}`}
                  className="inline-flex h-9 items-center rounded-lg bg-primary px-3 text-sm font-semibold text-white hover:bg-primary-800"
                >
                  모델 추가
                </Link>
              </div>
              {models.isPending ? (
                <AdminLoading />
              ) : models.isError ? (
                <ErrorState onRetry={() => models.refetch()} />
              ) : models.data.length === 0 ? (
                <p className="py-10 text-center text-gray-500">등록된 모델이 없습니다.</p>
              ) : (
                <ul className="divide-y divide-gray-100">
                  {models.data.map((model) => (
                    <li key={model.id}>
                      <Link to={`/admin/vehicles/models/${model.id}`} className="flex items-center gap-3 py-3 hover:bg-gray-50">
                        <span className="min-w-0 flex-1">
                          <span className="block font-semibold">{model.name}</span>
                          <span className="block text-xs text-gray-500">
                            {[model.segment, bodyTypeLabel[model.bodyType], fuelLabel[model.fuel]].filter(Boolean).join(' · ')}
                          </span>
                        </span>
                        <ActiveBadge active={model.active} />
                        <span aria-hidden="true" className="text-gray-400">
                          ›
                        </span>
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </section>
      </div>

      {editingBrand && (
        <BrandDialog
          key={editingBrand === 'new' ? 'new' : editingBrand.id}
          brand={editingBrand === 'new' ? undefined : editingBrand}
          onClose={() => setEditingBrand(null)}
          onSaved={(saved) => {
            setEditingBrand(null)
            setSearchParams({ brand: String(saved.id) }, { replace: true })
          }}
        />
      )}
    </div>
  )
}

function BrandDialog({ brand, onClose, onSaved }: { brand?: AdminBrand; onClose: () => void; onSaved: (brand: AdminBrand) => void }) {
  const [input, setInput] = useState<BrandInput>(
    brand ?? { name: '', origin: 'DOMESTIC', logoUrl: null, sortOrder: 0, active: true },
  )
  const save = useSaveBrand()
  const errors = fieldErrorMap(save.error)
  const message = formErrorMessage(save.error)

  return (
    <Modal open onClose={onClose} title={brand ? '브랜드 수정' : '브랜드 추가'} sheetOnMobile={false}>
      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault()
          save.mutate({ id: brand?.id, input }, { onSuccess: onSaved })
        }}
      >
        <TextField label="브랜드명" required value={input.name} error={errors.name} onChange={(e) => setInput({ ...input, name: e.target.value })} />
        <SelectField label="구분" required value={input.origin} error={errors.origin} onChange={(e) => setInput({ ...input, origin: e.target.value as BrandInput['origin'] })}>
          <option value="DOMESTIC">국산</option>
          <option value="IMPORTED">수입</option>
        </SelectField>
        <ImageUploadField label="로고" value={input.logoUrl} error={errors.logoUrl} previewClassName="h-24" onChange={(url) => setInput({ ...input, logoUrl: url })} />
        <TextField label="정렬 순서" type="number" min={0} value={input.sortOrder} hint="작은 숫자가 먼저 보입니다." onChange={(e) => setInput({ ...input, sortOrder: Number(e.target.value) || 0 })} />
        <ToggleField label="사용" description="끄면 사용자 화면에서 숨겨집니다." checked={input.active} onChange={(active) => setInput({ ...input, active })} />
        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>}
        <Button type="submit" fullWidth loading={save.isPending}>
          저장
        </Button>
      </form>
    </Modal>
  )
}
