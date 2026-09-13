package com.rentdb.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 사이트 공개 정보 (application.yml app.site.*, 환경변수로 교체).
 */
@ConfigurationProperties(prefix = "app.site")
public record SiteProperties(
		String name,
		String phone,
		String businessName,
		String representative,
		String businessNumber,
		String address) {
}
