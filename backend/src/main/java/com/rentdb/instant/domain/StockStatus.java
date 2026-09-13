package com.rentdb.instant.domain;

/** 즉시출고 재고 상태 */
public enum StockStatus {
	/** 판매중 */
	AVAILABLE,
	/** 예약중 */
	RESERVED,
	/** 판매완료 (목록 제외) */
	SOLD
}
