package com.rentdb.notification.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 실제로 보내지 않고 로그로만 남긴다 (app.notification.mail-type=log).
 * 본문에는 마스킹된 정보만 있으므로 로그에 남겨도 된다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.notification", name = "mail-type", havingValue = "log")
public class LogNotificationMailSender implements NotificationMailSender {

	@Override
	public void send(String to, String subject, String html) {
		log.info("[메일 로그 발송] to={}, subject={}", to, subject);
	}

}
