package com.rentdb.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.admin.* — 첫 관리자 계정 (관리자 계정이 하나도 없을 때만 사용)
 */
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String initialEmail, String initialPassword, String initialName) {
}
