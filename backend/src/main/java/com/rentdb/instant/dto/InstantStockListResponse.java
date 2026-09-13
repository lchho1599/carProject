package com.rentdb.instant.dto;

import java.util.List;

/** 즉시출고 목록 — totalCount는 "총 N대 대기중" 표시용 (현재 필터 기준) */
public record InstantStockListResponse(int totalCount, List<InstantStockResponse> items) {

	public static InstantStockListResponse of(List<InstantStockResponse> items) {
		return new InstantStockListResponse(items.size(), items);
	}

}
