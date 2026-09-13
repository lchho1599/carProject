package com.rentdb.lead.controller;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.global.common.PageResponse;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.global.security.AdminPrincipal;
import com.rentdb.lead.domain.LeadStatus;
import com.rentdb.lead.domain.LeadType;
import com.rentdb.lead.dto.admin.AdminLeadDetail;
import com.rentdb.lead.dto.admin.AdminLeadListItem;
import com.rentdb.lead.dto.admin.DashboardResponse;
import com.rentdb.lead.dto.admin.LeadNoteCreateRequest;
import com.rentdb.lead.dto.admin.LeadStatusUpdateRequest;
import com.rentdb.lead.repository.LeadSearch;
import com.rentdb.lead.service.AdminLeadService;
import com.rentdb.lead.support.LeadCsvWriter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** 관리자 리드 관리 API — 보안 필터 체인에서 ROLE_ADMIN 로그인과 CSRF 토큰을 확인한 뒤 도달한다 */
@Tag(name = "관리자 - 상담 신청")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLeadController {

	private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

	private final AdminLeadService adminLeadService;
	private final Clock clock;

	@Operation(summary = "대시보드 (오늘·이번주·이번달 신청 수, 상태별 건수, 최근 10건)")
	@GetMapping("/dashboard")
	public DashboardResponse dashboard() {
		return adminLeadService.dashboard();
	}

	@Operation(summary = "상담 신청 목록 (이름·연락처 마스킹)", description = "q: 숫자만 입력하면 연락처 일부, 그 밖에는 이름 일부로 검색")
	@GetMapping("/leads")
	public PageResponse<AdminLeadListItem> leads(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate to,
			@RequestParam(required = false) LeadType type,
			@RequestParam(required = false) LeadStatus status,
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return adminLeadService.search(new LeadSearch(from, to, type, status, q), page, size);
	}

	@Operation(summary = "상담 신청 목록 CSV 다운로드 (현재 필터 기준, 최대 1만 건, 다운로드 기록)")
	@GetMapping("/leads/export.csv")
	public ResponseEntity<byte[]> exportCsv(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate to,
			@RequestParam(required = false) LeadType type,
			@RequestParam(required = false) LeadStatus status,
			@RequestParam(required = false) String q,
			@AuthenticationPrincipal AdminPrincipal admin,
			HttpServletRequest request) {
		byte[] csv = LeadCsvWriter.write(adminLeadService.findForExport(
				new LeadSearch(from, to, type, status, q), admin.getId(), request));
		String filename = "leads-" + LocalDateTime.now(clock.withZone(TimeConfig.KST)).format(FILE_TIME) + ".csv";
		return ResponseEntity.ok()
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
				.header(HttpHeaders.CACHE_CONTROL, "no-store")
				.body(csv);
	}

	@Operation(summary = "상담 신청 상세 (이름·연락처 전체, 조회 기록)")
	@GetMapping("/leads/{leadId}")
	public AdminLeadDetail lead(@PathVariable Long leadId, @AuthenticationPrincipal AdminPrincipal admin,
			HttpServletRequest request) {
		return adminLeadService.getDetail(leadId, admin.getId(), request);
	}

	@Operation(summary = "상태 변경")
	@PatchMapping("/leads/{leadId}")
	public ResponseEntity<Void> changeStatus(@PathVariable Long leadId,
			@Valid @RequestBody LeadStatusUpdateRequest request) {
		adminLeadService.changeStatus(leadId, request.status());
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "메모 추가")
	@PostMapping("/leads/{leadId}/notes")
	public ResponseEntity<AdminLeadDetail.Note> addNote(@PathVariable Long leadId,
			@Valid @RequestBody LeadNoteCreateRequest request, @AuthenticationPrincipal AdminPrincipal admin) {
		return ResponseEntity.status(201).body(adminLeadService.addNote(leadId, admin.getId(), request.content()));
	}

}
