package com.rentdb.lead;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.lead.* — 중복 신청·요청 제한 설정
 */
@ConfigurationProperties(prefix = "app.lead")
public record LeadProperties(int duplicateWindowMinutes, RateLimit rateLimit) {

	public record RateLimit(int maxRequests, int windowMinutes) {
	}

}
