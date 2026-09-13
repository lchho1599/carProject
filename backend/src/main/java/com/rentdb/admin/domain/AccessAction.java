package com.rentdb.admin.domain;

/** 관리자 접근 기록 유형 */
public enum AccessAction {
	LOGIN,
	LOGIN_FAIL,
	/** 리드 상세(개인정보 전체) 조회 */
	LEAD_VIEW,
	/** 리드 CSV 다운로드 */
	LEAD_EXPORT,
	/** 관리자 추가 (target_id = 추가된 관리자) */
	ADMIN_CREATE,
	/** 관리자 이름 변경·활성화·비활성화 (target_id = 대상 관리자) */
	ADMIN_UPDATE,
	/** 본인 비밀번호 변경 */
	PASSWORD_CHANGE,
	/** 다른 관리자 비밀번호 재설정 (target_id = 대상 관리자) */
	PASSWORD_RESET
}
