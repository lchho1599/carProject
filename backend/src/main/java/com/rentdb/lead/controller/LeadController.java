package com.rentdb.lead.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.global.web.ClientInfoResolver;
import com.rentdb.lead.dto.LeadCreateRequest;
import com.rentdb.lead.dto.LeadCreateResponse;
import com.rentdb.lead.service.LeadCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "상담 신청")
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

	private final LeadCommandService leadCommandService;
	private final ClientInfoResolver clientInfoResolver;

	@Operation(summary = "상담 신청", description = "저장 후 운영자에게 알림 메일을 비동기로 발송한다 (발송 실패해도 신청은 성공)")
	@ApiResponse(responseCode = "201", description = "접수 완료")
	@ApiResponse(responseCode = "400", description = "입력값 오류 (VALIDATION_FAILED)")
	@ApiResponse(responseCode = "409", description = "같은 연락처로 최근 신청 이력 있음 (LEAD_DUPLICATED)")
	@ApiResponse(responseCode = "429", description = "요청 과다 (TOO_MANY_REQUESTS)")
	@PostMapping
	public ResponseEntity<LeadCreateResponse> create(@Valid @RequestBody LeadCreateRequest request,
			HttpServletRequest httpRequest) {
		LeadCreateResponse response = leadCommandService.create(request, clientInfoResolver.resolve(httpRequest));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
