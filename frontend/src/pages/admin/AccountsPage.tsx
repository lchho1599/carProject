import { useState, type FormEvent } from 'react'
import { AdminLoading, AdminPageHeader } from '../../components/admin/AdminPageStates'
import { ActiveBadge } from '../../components/admin/DisplayStatusBadge'
import { TextField } from '../../components/admin/form'
import { fieldErrorMap, formErrorMessage } from '../../components/admin/formUtils'
import { Button } from '../../components/ui/Button'
import { Modal } from '../../components/ui/Modal'
import { ErrorState } from '../../components/ui/StateViews'
import {
  useAdminAccounts,
  useChangeMyPassword,
  useCreateAdmin,
  useResetAdminPassword,
  useUpdateAdmin,
  type AdminAccount,
} from '../../features/admin/accounts'
import { newPasswordError, PASSWORD_RULE } from '../../features/admin/password'
import { formatDateTime } from '../../lib/format'

type Dialog = { kind: 'create' } | { kind: 'rename'; account: AdminAccount } | { kind: 'reset'; account: AdminAccount }

/** 관리자 계정 관리 — 내 비밀번호 변경, 관리자 추가·이름 변경·비활성화·비밀번호 재설정 */
export function AccountsPage() {
  const accounts = useAdminAccounts()
  const update = useUpdateAdmin()
  const [dialog, setDialog] = useState<Dialog | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const toggleActive = (account: AdminAccount) => {
    const next = !account.active
    const question = next
      ? `${account.name}(${account.email}) 계정을 다시 사용하도록 할까요?`
      : `${account.name}(${account.email}) 계정을 비활성화할까요?\n로그인 중이면 즉시 로그아웃되고, 다시 활성화하기 전까지 로그인할 수 없습니다.`
    if (!window.confirm(question)) return
    setNotice(null)
    update.mutate(
      { id: account.id, name: account.name, active: next },
      { onSuccess: () => setNotice(next ? '계정을 활성화했습니다.' : '계정을 비활성화하고 로그인 세션을 종료했습니다.') },
    )
  }

  return (
    <div className="space-y-4">
      <AdminPageHeader
        title="관리자 계정"
        actions={
          <Button size="sm" onClick={() => setDialog({ kind: 'create' })}>
            관리자 추가
          </Button>
        }
      />

      <MyPasswordCard />

      <section aria-label="관리자 목록" className="rounded-xl bg-white p-4 md:p-5">
        <h2 className="mb-1 font-extrabold">관리자 목록</h2>
        <p className="mb-3 text-sm text-gray-500">본인 계정은 비활성화할 수 없습니다. 비활성화하거나 비밀번호를 재설정하면 해당 관리자는 즉시 로그아웃됩니다.</p>
        {notice && <p role="status" className="mb-3 rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">{notice}</p>}
        {update.isError && <p role="alert" className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{formErrorMessage(update.error)}</p>}

        {accounts.isPending ? (
          <AdminLoading />
        ) : accounts.isError ? (
          <ErrorState onRetry={() => accounts.refetch()} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[760px] text-sm">
              <thead className="text-left text-gray-500">
                <tr>
                  <th className="py-2 pr-3 font-semibold">이름</th>
                  <th className="px-3 font-semibold">이메일</th>
                  <th className="px-3 font-semibold">상태</th>
                  <th className="px-3 font-semibold">마지막 로그인</th>
                  <th className="px-3 font-semibold">등록일</th>
                  <th className="pl-3 text-right font-semibold">관리</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {accounts.data.map((account) => (
                  <tr key={account.id} className={account.active ? '' : 'text-gray-400'}>
                    <td className="py-3 pr-3 font-semibold">
                      {account.name}
                      {account.me && <span className="ml-1 rounded bg-primary-50 px-1.5 py-0.5 text-xs text-primary-700">나</span>}
                    </td>
                    <td className="px-3">{account.email}</td>
                    <td className="px-3">
                      <ActiveBadge active={account.active} />
                    </td>
                    <td className="px-3 whitespace-nowrap">{account.lastLoginAt ? formatDateTime(account.lastLoginAt) : '-'}</td>
                    <td className="px-3 whitespace-nowrap">{formatDateTime(account.createdAt)}</td>
                    <td className="pl-3">
                      <div className="flex justify-end gap-1">
                        <Button size="sm" variant="ghost" aria-label={`${account.name} 이름 변경`} onClick={() => setDialog({ kind: 'rename', account })}>
                          이름 변경
                        </Button>
                        {!account.me && (
                          <>
                            <Button size="sm" variant="ghost" aria-label={`${account.name} 비밀번호 재설정`} onClick={() => setDialog({ kind: 'reset', account })}>
                              비밀번호 재설정
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className={account.active ? 'text-red-600' : ''}
                              aria-label={`${account.name} ${account.active ? '비활성화' : '활성화'}`}
                              loading={update.isPending && update.variables?.id === account.id}
                              onClick={() => toggleActive(account)}
                            >
                              {account.active ? '비활성화' : '활성화'}
                            </Button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {dialog?.kind === 'create' && (
        <CreateAdminDialog
          onClose={() => setDialog(null)}
          onCreated={(account) => {
            setDialog(null)
            setNotice(`${account.name}(${account.email}) 관리자를 추가했습니다. 초기 비밀번호를 안전한 방법으로 전달해 주세요.`)
          }}
        />
      )}
      {dialog?.kind === 'rename' && <RenameDialog account={dialog.account} onClose={() => setDialog(null)} />}
      {dialog?.kind === 'reset' && (
        <ResetPasswordDialog
          account={dialog.account}
          onClose={() => setDialog(null)}
          onDone={() => {
            setDialog(null)
            setNotice(`${dialog.account.name} 관리자의 비밀번호를 재설정하고 로그인 세션을 종료했습니다.`)
          }}
        />
      )}
    </div>
  )
}

function MyPasswordCard() {
  const change = useChangeMyPassword()
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [clientError, setClientError] = useState<string | null>(null)
  const [done, setDone] = useState(false)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    setDone(false)
    const error = form.currentPassword ? newPasswordError(form.newPassword, form.confirm) : '현재 비밀번호를 입력해 주세요.'
    setClientError(error)
    if (error) return
    change.mutate(
      { currentPassword: form.currentPassword, newPassword: form.newPassword },
      {
        onSuccess: () => {
          setForm({ currentPassword: '', newPassword: '', confirm: '' })
          setDone(true)
        },
      },
    )
  }

  const message = clientError ?? formErrorMessage(change.error)
  return (
    <section aria-label="내 비밀번호 변경" className="rounded-xl bg-white p-4 md:p-5">
      <h2 className="mb-1 font-extrabold">내 비밀번호 변경</h2>
      <p className="mb-3 text-sm text-gray-500">변경하면 지금 사용 중인 브라우저를 제외한 다른 기기의 로그인은 종료됩니다.</p>
      <form onSubmit={submit} noValidate className="grid gap-3 md:grid-cols-4 md:items-end">
        <TextField label="현재 비밀번호" type="password" autoComplete="current-password" value={form.currentPassword} onChange={(e) => setForm({ ...form, currentPassword: e.target.value })} />
        <TextField label="새 비밀번호" type="password" autoComplete="new-password" hint={PASSWORD_RULE} value={form.newPassword} onChange={(e) => setForm({ ...form, newPassword: e.target.value })} />
        <TextField label="새 비밀번호 확인" type="password" autoComplete="new-password" value={form.confirm} onChange={(e) => setForm({ ...form, confirm: e.target.value })} />
        <Button type="submit" loading={change.isPending} className="md:mb-5">
          비밀번호 변경
        </Button>
      </form>
      {message && <p role="alert" className="mt-2 text-sm text-red-600">{message}</p>}
      {done && <p role="status" className="mt-2 text-sm text-green-700">비밀번호를 변경했습니다.</p>}
    </section>
  )
}

function CreateAdminDialog({ onClose, onCreated }: { onClose: () => void; onCreated: (account: AdminAccount) => void }) {
  const create = useCreateAdmin()
  const [form, setForm] = useState({ email: '', name: '', password: '', confirm: '' })
  const [clientError, setClientError] = useState<string | null>(null)
  const errors = fieldErrorMap(create.error)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const error = newPasswordError(form.password, form.confirm)
    setClientError(error)
    if (error) return
    create.mutate({ email: form.email, name: form.name, password: form.password }, { onSuccess: onCreated })
  }

  const message = clientError ?? formErrorMessage(create.error)
  return (
    <Modal open onClose={onClose} title="관리자 추가" sheetOnMobile={false}>
      <form onSubmit={submit} noValidate className="space-y-3">
        <TextField label="이메일" required type="email" autoComplete="off" value={form.email} error={errors.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        <TextField label="이름" required value={form.name} error={errors.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <TextField label="초기 비밀번호" required type="password" autoComplete="new-password" hint={PASSWORD_RULE} value={form.password} error={errors.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        <TextField label="초기 비밀번호 확인" required type="password" autoComplete="new-password" value={form.confirm} onChange={(e) => setForm({ ...form, confirm: e.target.value })} />
        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>}
        <Button type="submit" fullWidth loading={create.isPending}>
          추가
        </Button>
      </form>
    </Modal>
  )
}

function RenameDialog({ account, onClose }: { account: AdminAccount; onClose: () => void }) {
  const update = useUpdateAdmin()
  const [name, setName] = useState(account.name)
  const errors = fieldErrorMap(update.error)
  const message = formErrorMessage(update.error)

  return (
    <Modal open onClose={onClose} title="이름 변경" sheetOnMobile={false}>
      <form
        className="space-y-3"
        onSubmit={(event) => {
          event.preventDefault()
          update.mutate({ id: account.id, name, active: account.active }, { onSuccess: onClose })
        }}
      >
        <p className="text-sm text-gray-500">{account.email}</p>
        <TextField label="이름" required value={name} error={errors.name} onChange={(e) => setName(e.target.value)} />
        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>}
        <Button type="submit" fullWidth loading={update.isPending}>
          저장
        </Button>
      </form>
    </Modal>
  )
}

function ResetPasswordDialog({ account, onClose, onDone }: { account: AdminAccount; onClose: () => void; onDone: () => void }) {
  const reset = useResetAdminPassword()
  const [form, setForm] = useState({ newPassword: '', confirm: '' })
  const [clientError, setClientError] = useState<string | null>(null)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const error = newPasswordError(form.newPassword, form.confirm)
    setClientError(error)
    if (error) return
    reset.mutate({ id: account.id, newPassword: form.newPassword }, { onSuccess: onDone })
  }

  const message = clientError ?? formErrorMessage(reset.error)
  return (
    <Modal open onClose={onClose} title="비밀번호 재설정" sheetOnMobile={false}>
      <form onSubmit={submit} noValidate className="space-y-3">
        <p className="text-sm text-gray-600">
          <strong>{account.name}</strong>({account.email}) 관리자의 비밀번호를 새로 정합니다. 해당 관리자는 즉시 로그아웃됩니다.
        </p>
        <TextField label="새 비밀번호" required type="password" autoComplete="new-password" hint={PASSWORD_RULE} value={form.newPassword} onChange={(e) => setForm({ ...form, newPassword: e.target.value })} />
        <TextField label="새 비밀번호 확인" required type="password" autoComplete="new-password" value={form.confirm} onChange={(e) => setForm({ ...form, confirm: e.target.value })} />
        {message && <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>}
        <Button type="submit" fullWidth loading={reset.isPending}>
          재설정
        </Button>
      </form>
    </Modal>
  )
}
