package com.rentdb.admin.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.admin.dto.AdminMeResponse;
import com.rentdb.global.security.AdminPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 로그인(POST /api/admin/auth/login)과 로그아웃(POST /api/admin/auth/logout)은 보안 필터가 처리한다.
 * 여기서는 현재 로그인 상태 확인만 제공한다.
 */
@Tag(name = "관리자 인증")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

	@Operation(summary = "로그인한 관리자 정보 + CSRF 토큰 (로그인하지 않았으면 401)")
	@GetMapping("/me")
	public AdminMeResponse me(@AuthenticationPrincipal AdminPrincipal principal, CsrfToken csrfToken) {
		return AdminMeResponse.of(principal, csrfToken.getToken());
	}

}
