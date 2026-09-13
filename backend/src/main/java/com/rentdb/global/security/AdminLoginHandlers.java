package com.rentdb.global.security;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.rentdb.admin.domain.AccessAction;
import com.rentdb.admin.dto.AdminMeResponse;
import com.rentdb.admin.repository.AdminUserRepository;
import com.rentdb.admin.service.AdminAccessLogService;
import com.rentdb.global.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 성공·실패 응답 — JSON으로 응답하고 접근 기록(LOGIN / LOGIN_FAIL)을 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoginHandlers implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

	private final AdminUserRepository adminUserRepository;
	private final AdminAccessLogService accessLogService;
	private final SecurityErrorWriter errorWriter;
	private final TransactionTemplate transactionTemplate;
	private final Clock clock;

	/** 성공: 마지막 로그인 시각 갱신 + 기록 + { id, email, name, csrfToken } */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		AdminPrincipal principal = (AdminPrincipal) authentication.getPrincipal();

		transactionTemplate.executeWithoutResult(status -> adminUserRepository.findById(principal.getId())
				.ifPresent(admin -> admin.recordLogin(OffsetDateTime.now(clock))));
		accessLogService.record(principal.getId(), AccessAction.LOGIN, null, request);

		// 세션 인증 전략(CsrfAuthenticationStrategy)이 로그인 직후 새 토큰을 요청 속성에 넣어 둔다
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		errorWriter.writeJson(response, HttpServletResponse.SC_OK,
				AdminMeResponse.of(principal, csrfToken == null ? null : csrfToken.getToken()));
	}

	/** 실패: 계정 존재 여부를 알 수 없도록 항상 같은 메시지 */
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		log.info("관리자 로그인 실패: {}", exception.getClass().getSimpleName());
		accessLogService.record(null, AccessAction.LOGIN_FAIL, null, request);
		errorWriter.write(response, ErrorCode.LOGIN_FAILED);
	}

}
