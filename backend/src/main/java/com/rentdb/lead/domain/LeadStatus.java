package com.rentdb.lead.domain;

/** 리드 처리 상태 */
public enum LeadStatus {
	/** 신규 */
	NEW,
	/** 상담중 */
	IN_PROGRESS,
	/** 계약완료 */
	CONTRACTED,
	/** 부재 */
	NO_ANSWER,
	/** 취소 */
	CANCELED
}
