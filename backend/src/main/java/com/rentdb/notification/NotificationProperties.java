package com.rentdb.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.notification.* — 메일 발송 방식, 보내는 주소, 관리자 링크 도메인
 */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(String mailType, String from, String adminBaseUrl) {
}
