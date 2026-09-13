import { Modal } from '../ui/Modal'

type Props = { open: boolean; onClose: () => void }

/**
 * 개인정보 수집·이용 동의 내용.
 * 임시 문구 — 배포 전 개인정보처리방침과 함께 최종 문구로 교체한다(전문가 검토 권장).
 */
export function AgreementModal({ open, onClose }: Props) {
  return (
    <Modal open={open} onClose={onClose} title="개인정보 수집·이용 동의" sheetOnMobile={false}>
      <table className="w-full border-collapse text-sm">
        <tbody>
          <tr className="border-b border-gray-100">
            <th className="w-24 bg-gray-50 px-3 py-2 text-left font-semibold">수집 항목</th>
            <td className="px-3 py-2">이름, 휴대폰 번호, 상담 차량 및 이용조건</td>
          </tr>
          <tr className="border-b border-gray-100">
            <th className="bg-gray-50 px-3 py-2 text-left font-semibold">이용 목적</th>
            <td className="px-3 py-2">장기렌트·리스 견적 안내 및 상담 연락</td>
          </tr>
          <tr className="border-b border-gray-100">
            <th className="bg-gray-50 px-3 py-2 text-left font-semibold">보유 기간</th>
            <td className="px-3 py-2">신청일로부터 1년 (기간 경과 후 파기)</td>
          </tr>
        </tbody>
      </table>
      <p className="mt-4 text-sm text-gray-600">
        동의를 거부할 수 있으며, 거부하시면 상담 신청이 제한됩니다.
      </p>
      <p className="mt-2 text-xs text-gray-400">※ 임시 문구입니다. 정식 오픈 전에 최종 문구로 교체됩니다.</p>
    </Modal>
  )
}
