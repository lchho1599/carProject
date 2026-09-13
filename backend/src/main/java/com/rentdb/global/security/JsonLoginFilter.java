package com.rentdb.global.security;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.rentdb.admin.domain.AdminUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * 관리자 로그인 필터 — POST /api/admin/auth/login { "email", "password" } (JSON).
 * 기본 폼 로그인(form-data) 대신 SPA에 맞춰 JSON을 받는다.
 * 인증 성공 후 세션 ID 교체·CSRF 토큰 재발급·세션 저장은 SecurityConfig 에서 설정한 전략이 처리한다.
 */
public class JsonLoginFilter extends AbstractAuthenticationProcessingFilter {

	public static final String LOGIN_URL = "/api/admin/auth/login";
	private static final int MAX_PASSWORD_LENGTH = 100;

	private final JsonMapper jsonMapper;

	public JsonLoginFilter(AuthenticationManager authenticationManager, JsonMapper jsonMapper) {
		super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, LOGIN_URL));
		setAuthenticationManager(authenticationManager);
		this.jsonMapper = jsonMapper;
	}

	record LoginRequest(String email, String password) {
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException {
		LoginRequest login;
		try {
			login = jsonMapper.readValue(request.getInputStream(), LoginRequest.class);
		}
		catch (RuntimeException e) {
			throw new AuthenticationServiceException("로그인 요청 형식이 올바르지 않습니다.", e);
		}

		if (login == null || isBlank(login.email()) || isBlank(login.password())
				|| login.password().length() > MAX_PASSWORD_LENGTH) {
			throw new BadCredentialsException("이메일 또는 비밀번호 누락");
		}

		UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken
				.unauthenticated(AdminUser.normalizeEmail(login.email()), login.password());
		token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		return getAuthenticationManager().authenticate(token);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
