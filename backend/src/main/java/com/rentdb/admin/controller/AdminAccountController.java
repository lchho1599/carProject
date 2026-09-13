package com.rentdb.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.admin.dto.AdminAccountDtos.AccountResponse;
import com.rentdb.admin.dto.AdminAccountDtos.CreateRequest;
import com.rentdb.admin.dto.AdminAccountDtos.PasswordChangeRequest;
import com.rentdb.admin.dto.AdminAccountDtos.PasswordResetRequest;
import com.rentdb.admin.dto.AdminAccountDtos.UpdateRequest;
import com.rentdb.admin.service.AdminAccountService;
import com.rentdb.global.security.AdminPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 - 계정")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminAccountController {

	private final AdminAccountService service;

	@Operation(summary = "관리자 목록")
	@GetMapping
	public List<AccountResponse> accounts(@AuthenticationPrincipal AdminPrincipal me) {
		return service.getAccounts(me.getId());
	}

	@Operation(summary = "관리자 추가 (비밀번호: 8~100자, 영문+숫자)")
	@PostMapping
	public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateRequest request,
			@AuthenticationPrincipal AdminPrincipal me, HttpServletRequest httpRequest) {
		return ResponseEntity.status(201).body(service.create(request, me.getId(), httpRequest));
	}

	@Operation(summary = "이름 변경·활성화·비활성화 (본인 비활성화 불가, 비활성화 시 세션 종료)")
	@PutMapping("/{adminId}")
	public AccountResponse update(@PathVariable Long adminId, @Valid @RequestBody UpdateRequest request,
			@AuthenticationPrincipal AdminPrincipal me, HttpServletRequest httpRequest) {
		return service.update(adminId, request, me.getId(), httpRequest);
	}

	@Operation(summary = "내 비밀번호 변경 (다른 기기 세션 종료)")
	@PutMapping("/me/password")
	public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody PasswordChangeRequest request,
			@AuthenticationPrincipal AdminPrincipal me, HttpServletRequest httpRequest) {
		service.changeMyPassword(me.getId(), request.currentPassword(), request.newPassword(), httpRequest);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "다른 관리자 비밀번호 재설정 (대상 세션 종료)")
	@PutMapping("/{adminId}/password")
	public ResponseEntity<Void> resetPassword(@PathVariable Long adminId,
			@Valid @RequestBody PasswordResetRequest request, @AuthenticationPrincipal AdminPrincipal me,
			HttpServletRequest httpRequest) {
		service.resetPassword(adminId, request.newPassword(), me.getId(), httpRequest);
		return ResponseEntity.noContent().build();
	}

}
