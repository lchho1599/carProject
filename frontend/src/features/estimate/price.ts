import type { ModelDetail } from '../vehicle/types'

export type EstimateSelection = {
  trimId: number | undefined
  colorId: number | undefined
  optionIds: number[]
}

export type EstimatePrice = {
  trimPrice: number
  colorPrice: number
  optionPrice: number
  total: number
}

/**
 * 화면 표시용 합계 차량가 = 트림가 + 색상 추가금 + 선택 옵션가.
 * 실제 저장되는 금액은 서버가 같은 규칙으로 다시 계산한다 (화면 값은 신뢰하지 않음).
 */
export function calculateEstimatePrice(model: ModelDetail, selection: EstimateSelection): EstimatePrice {
  const trimPrice = model.trims.find((trim) => trim.id === selection.trimId)?.price ?? 0
  const colorPrice = model.colors.find((color) => color.id === selection.colorId)?.extraPrice ?? 0
  const optionPrice = model.options
    .filter((option) => selection.optionIds.includes(option.id))
    .reduce((sum, option) => sum + option.price, 0)

  return { trimPrice, colorPrice, optionPrice, total: trimPrice + colorPrice + optionPrice }
}

/** 주소의 ?trimId= 가 이 모델의 트림이면 그 값, 아니면 첫 번째 트림 */
export function initialTrimId(model: ModelDetail, trimIdParam: string | null): number | undefined {
  const requested = Number(trimIdParam)
  if (trimIdParam && model.trims.some((trim) => trim.id === requested)) return requested
  return model.trims[0]?.id
}
