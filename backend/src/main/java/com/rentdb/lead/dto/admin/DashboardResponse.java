package com.rentdb.lead.dto.admin;

import java.util.List;
import java.util.Map;

import com.rentdb.lead.domain.LeadStatus;

/** 관리자 대시보드 — 기간별 신청 수(한국 시간 기준), 상태별 건수, 최근 신청 10건 */
public record DashboardResponse(
		long todayCount,
		long weekCount,
		long monthCount,
		long totalCount,
		Map<LeadStatus, Long> statusCounts,
		List<AdminLeadListItem> recentLeads) {
}
