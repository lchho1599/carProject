import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { LeadContext } from '../../features/lead/types'
import { LeadForm } from './LeadForm'

const fetchMock = vi.fn()

function jsonResponse(status: number, body: unknown) {
  return Promise.resolve(new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }))
}

function renderForm(context: LeadContext = { type: 'QUICK' }) {
  const router = createMemoryRouter(
    [
      { path: '/', element: <LeadForm context={context} /> },
      { path: '/complete', element: <p>신청 완료 화면</p> },
    ],
    { initialEntries: ['/'] },
  )
  const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
  return { user: userEvent.setup() }
}

async function fillValid(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('이름'), '홍길동')
  await user.type(screen.getByLabelText('연락처'), '01012345678')
  await user.click(screen.getByRole('checkbox', { name: /개인정보 수집·이용 동의/ }))
}

describe('상담신청 폼', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    fetchMock.mockReset()
    vi.unstubAllGlobals()
  })

  it('빈 값으로 제출하면 항목별 오류를 보여주고 전송하지 않는다', async () => {
    const { user } = renderForm()

    await user.click(screen.getByRole('button', { name: '무료 상담 신청' }))

    expect(await screen.findByText('이름을 2자 이상 입력해 주세요.')).toBeInTheDocument()
    expect(screen.getByText('연락처를 입력해 주세요.')).toBeInTheDocument()
    expect(screen.getByText('개인정보 수집·이용에 동의해 주세요.')).toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('한 화면에 폼이 두 개 있어도 라벨이 각자의 입력칸에 연결된다', () => {
    const router = createMemoryRouter(
      [
        {
          path: '/',
          element: (
            <>
              <LeadForm context={{ type: 'QUICK' }} />
              <LeadForm context={{ type: 'QUICK' }} />
            </>
          ),
        },
      ],
      { initialEntries: ['/'] },
    )
    render(
      <QueryClientProvider client={new QueryClient()}>
        <RouterProvider router={router} />
      </QueryClientProvider>,
    )

    const phoneInputs = screen.getAllByLabelText('연락처')
    expect(phoneInputs).toHaveLength(2)
    expect(phoneInputs[0]).not.toBe(phoneInputs[1])
    expect(phoneInputs[0].id).not.toBe(phoneInputs[1].id)
  })

  it('연락처를 입력하면 하이픈이 자동으로 들어간다', async () => {
    const { user } = renderForm()

    await user.type(screen.getByLabelText('연락처'), '01012345678')

    expect(screen.getByLabelText('연락처')).toHaveValue('010-1234-5678')
  })

  it('정상 제출하면 차량 정보·utm 과 함께 전송하고 완료 화면으로 이동한다', async () => {
    sessionStorage.setItem('rentdb.utm', JSON.stringify({ utm_source: 'naver' }))
    fetchMock.mockReturnValue(jsonResponse(201, { id: 1 }))
    const { user } = renderForm({ type: 'DEAL', dealId: 7, label: '기아 쏘렌토' })

    await fillValid(user)
    await user.click(screen.getByRole('button', { name: '무료 상담 신청' }))

    expect(await screen.findByText('신청 완료 화면')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/leads')
    expect(init.method).toBe('POST')
    const body = JSON.parse(init.body)
    expect(body).toMatchObject({
      type: 'DEAL',
      dealId: 7,
      name: '홍길동',
      phone: '010-1234-5678',
      agreePrivacy: true,
      agreeMarketing: false,
      utm: { utm_source: 'naver' },
    })
    expect(body).not.toHaveProperty('label')
  })

  it('중복 신청(409)이면 서버 안내 문구를 보여주고 화면을 유지한다', async () => {
    fetchMock.mockReturnValue(
      jsonResponse(409, { code: 'LEAD_DUPLICATED', message: '이미 상담 신청이 접수되었습니다.', fieldErrors: [] }),
    )
    const { user } = renderForm()

    await fillValid(user)
    await user.click(screen.getByRole('button', { name: '무료 상담 신청' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('이미 상담 신청이 접수되었습니다.')
    expect(screen.queryByText('신청 완료 화면')).not.toBeInTheDocument()
  })

  it('서버가 항목 오류를 주면 해당 입력칸 아래에 표시한다', async () => {
    fetchMock.mockReturnValue(
      jsonResponse(400, {
        code: 'VALIDATION_FAILED',
        message: '요청 값이 올바르지 않습니다.',
        fieldErrors: [{ field: 'phone', message: '서버: 휴대폰 번호 형식 오류' }],
      }),
    )
    const { user } = renderForm()

    await fillValid(user)
    await user.click(screen.getByRole('button', { name: '무료 상담 신청' }))

    await waitFor(() => expect(screen.getByText('서버: 휴대폰 번호 형식 오류')).toBeInTheDocument())
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })
})
