package com.rentdb.lead.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.admin.domain.AccessAction;
import com.rentdb.admin.repository.AdminUserRepository;
import com.rentdb.admin.service.AdminAccessLogService;
import com.rentdb.global.common.PageResponse;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.global.error.BusinessException;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.domain.LeadNote;
import com.rentdb.lead.domain.LeadStatus;
import com.rentdb.lead.dto.admin.AdminLeadDetail;
import com.rentdb.lead.dto.admin.AdminLeadListItem;
import com.rentdb.lead.dto.admin.DashboardResponse;
import com.rentdb.lead.repository.LeadNoteRepository;
import com.rentdb.lead.repository.LeadRepository;
import com.rentdb.lead.repository.LeadSearch;
import com.rentdb.notification.repository.NotificationLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 리드 관리 — 목록(마스킹), 상세(전체 정보 + 조회 기록), 상태 변경, 메모, 대시보드, CSV.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLeadService {

	public static final int MAX_PAGE_SIZE = 100;
	public static final int MAX_EXPORT_ROWS = 10_000;

	private final LeadRepository leadRepository;
	private final LeadNoteRepository leadNoteRepository;
	private final NotificationLogRepository notificationLogRepository;
	private final AdminUserRepository adminUserRepository;
	private final AdminAccessLogService accessLogService;
	private final Clock clock;

	public PageResponse<AdminLeadListItem> search(LeadSearch search, int page, int size) {
		// 정렬은 QueryDSL 쿼리에서 최신순(createdAt desc, id desc)으로 고정한다
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
		Page<Lead> result = leadRepository.search(search, pageable);
		return PageResponse.of(result, AdminLeadListItem::from);
	}

	/** 상세 조회 — 이름·연락처 전체가 나가므로 조회 기록을 남긴다 */
	public AdminLeadDetail getDetail(Long leadId, Long adminId, HttpServletRequest request) {
		Lead lead = findLead(leadId);
		AdminLeadDetail detail = AdminLeadDetail.of(
				lead,
				leadNoteRepository.findByLeadIdOrderByCreatedAtDescIdDesc(leadId),
				notificationLogRepository.findByLeadIdOrderByIdAsc(leadId));
		accessLogService.record(adminId, AccessAction.LEAD_VIEW, leadId, request);
		return detail;
	}

	@Transactional
	public void changeStatus(Long leadId, LeadStatus status) {
		findLead(leadId).changeStatus(status);
	}

	@Transactional
	public AdminLeadDetail.Note addNote(Long leadId, Long adminId, String content) {
		Lead lead = findLead(leadId);
		LeadNote note = leadNoteRepository.save(LeadNote.builder()
				.lead(lead)
				.admin(adminUserRepository.getReferenceById(adminId))
				.content(content.strip())
				.build());
		String adminName = adminUserRepository.findById(adminId).map(admin -> admin.getName()).orElse("");
		return new AdminLeadDetail.Note(note.getId(), note.getContent(), adminName, TimeConfig.toKst(note.getCreatedAt()));
	}

	public DashboardResponse dashboard() {
		LocalDate today = LocalDate.now(clock.withZone(TimeConfig.KST));
		OffsetDateTime todayStart = startOf(today);
		OffsetDateTime weekStart = startOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
		OffsetDateTime monthStart = startOf(today.withDayOfMonth(1));

		Map<LeadStatus, Long> statusCounts = new EnumMap<>(LeadStatus.class);
		for (LeadStatus status : LeadStatus.values()) {
			statusCounts.put(status, 0L);
		}
		for (Object[] row : leadRepository.countGroupByStatus()) {
			statusCounts.put((LeadStatus) row[0], (Long) row[1]);
		}

		return new DashboardResponse(
				leadRepository.countByCreatedAtGreaterThanEqual(todayStart),
				leadRepository.countByCreatedAtGreaterThanEqual(weekStart),
				leadRepository.countByCreatedAtGreaterThanEqual(monthStart),
				statusCounts.values().stream().mapToLong(Long::longValue).sum(),
				statusCounts,
				leadRepository.findTop10ByOrderByCreatedAtDescIdDesc().stream().map(AdminLeadListItem::from).toList());
	}

	/** CSV 다운로드용 — 최신순 최대 1만 건, 다운로드 기록을 남긴다 */
	public List<Lead> findForExport(LeadSearch search, Long adminId, HttpServletRequest request) {
		List<Lead> leads = leadRepository.searchForExport(search, MAX_EXPORT_ROWS);
		accessLogService.record(adminId, AccessAction.LEAD_EXPORT, null, request);
		return leads;
	}

	private Lead findLead(Long leadId) {
		return leadRepository.findById(leadId).orElseThrow(() -> BusinessException.notFound("상담 신청"));
	}

	private static OffsetDateTime startOf(LocalDate date) {
		return date.atStartOfDay(TimeConfig.KST).toOffsetDateTime();
	}

}
