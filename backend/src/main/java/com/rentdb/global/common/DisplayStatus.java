package com.rentdb.global.common;

import java.time.OffsetDateTime;

/** 관리자 목록의 노출 상태 — 공개 여부·노출 기간으로 계산 */
public enum DisplayStatus {
	/** 사용자 화면에 노출 중 */
	VISIBLE,
	/** 시작 전 */
	SCHEDULED,
	/** 종료됨 */
	ENDED,
	/** 비공개 (또는 차량이 비활성) */
	HIDDEN;

	public static DisplayStatus of(boolean published, OffsetDateTime startsAt, OffsetDateTime endsAt,
			OffsetDateTime now) {
		if (!published) {
			return HIDDEN;
		}
		if (startsAt != null && startsAt.isAfter(now)) {
			return SCHEDULED;
		}
		if (endsAt != null && !endsAt.isAfter(now)) {
			return ENDED;
		}
		return VISIBLE;
	}

}
