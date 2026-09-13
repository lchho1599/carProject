package com.rentdb.global.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rentdb.global.error.ErrorCode;
import com.rentdb.global.ratelimit.SlidingWindowRateLimiter;
import com.rentdb.global.web.ClientInfoResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 보안 필터 체인 안에서 동작하는 IP당 요청 제한.
 * 규칙마다 대상 요청(메서드+경로)과 제한기를 따로 둔다 — 예: 상담 신청, 관리자 로그인.
 * 스프링 빈으로 등록하지 않는다(등록하면 서블릿 필터로도 한 번 더 걸리므로 SecurityConfig 에서 직접 생성).
 */
public class RateLimitFilter extends OncePerRequestFilter {

	public record Rule(String name, RequestMatcher matcher, SlidingWindowRateLimiter limiter) {
	}

	private final List<Rule> rules;
	private final ClientInfoResolver clientInfoResolver;
	private final SecurityErrorWriter errorWriter;

	public RateLimitFilter(List<Rule> rules, ClientInfoResolver clientInfoResolver, SecurityErrorWriter errorWriter) {
		this.rules = rules;
		this.clientInfoResolver = clientInfoResolver;
		this.errorWriter = errorWriter;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		for (Rule rule : rules) {
			if (rule.matcher().matches(request)) {
				String key = clientInfoResolver.resolve(request).ipHash();
				if (!rule.limiter().tryAcquire(key)) {
					logger.warn("요청 제한 초과: rule=" + rule.name());
					response.setHeader("Retry-After", "600");
					errorWriter.write(response, ErrorCode.TOO_MANY_REQUESTS);
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}

}
