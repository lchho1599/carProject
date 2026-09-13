package com.rentdb.global.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.security.* — IP 해시 솔트, CORS 허용 주소, 로그인 시도 제한
 */
@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(String ipHashSalt, String corsAllowedOrigins, RateLimit loginRateLimit,
		boolean sessionCookieSecure) {

	public record RateLimit(int maxRequests, int windowMinutes) {
	}

	/** "https://a.com, https://b.com" → [https://a.com, https://b.com] */
	public List<String> corsAllowedOriginList() {
		if (corsAllowedOrigins == null || corsAllowedOrigins.isBlank()) {
			return List.of();
		}
		return Arrays.stream(corsAllowedOrigins.split(","))
				.map(String::strip)
				.filter(origin -> !origin.isEmpty())
				.toList();
	}

}
