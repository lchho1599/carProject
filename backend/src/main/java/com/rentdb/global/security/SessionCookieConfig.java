package com.rentdb.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * 관리자 세션 쿠키 — Spring Boot 4 에서는 server.servlet.session.cookie.* 가 Spring Session 쿠키에
 * 적용되지 않으므로 직접 설정한다 (AdminSecurityTest 에서 속성을 검증).
 */
@Configuration
public class SessionCookieConfig {

	public static final String SESSION_COOKIE_NAME = "RENTDB_SESSION";

	@Bean
	public CookieSerializer cookieSerializer(AppSecurityProperties securityProperties) {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName(SESSION_COOKIE_NAME);
		serializer.setCookiePath("/");
		serializer.setUseHttpOnlyCookie(true);                              // 스크립트에서 쿠키 접근 차단
		serializer.setSameSite("Lax");                                     // 다른 사이트에서 오는 변경 요청에 쿠키 미전송
		serializer.setUseSecureCookie(securityProperties.sessionCookieSecure()); // 운영: HTTPS 에서만 전송
		return serializer;
	}

}
