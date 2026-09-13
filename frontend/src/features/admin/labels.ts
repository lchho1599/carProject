import type { StockStatus } from '../instant/api'
import type { DisplayStatus } from './catalog'

export const displayStatusLabel: Record<DisplayStatus, string> = {
  VISIBLE: '노출중',
  SCHEDULED: '노출예정',
  ENDED: '기간종료',
  HIDDEN: '비공개',
}

export const stockStatusLabel: Record<StockStatus, string> = {
  AVAILABLE: '판매중',
  RESERVED: '예약중',
  SOLD: '판매완료',
}
