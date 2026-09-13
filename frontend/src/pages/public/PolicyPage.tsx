// 이용약관·개인정보처리방침 — 최종 문구는 배포 전에 받아서 교체한다 (전문가 검토 권장)
export function PolicyPage({ title }: { title: string }) {
  return (
    <section className="mx-auto max-w-3xl px-4 py-10">
      <h1 className="text-2xl font-extrabold">{title}</h1>
      <p className="mt-4 rounded-lg bg-amber-50 px-4 py-3 text-sm text-amber-800">
        정식 문구를 준비 중입니다. 오픈 전에 최종 내용으로 교체됩니다.
      </p>
    </section>
  )
}
