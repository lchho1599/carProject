package com.rentdb.notification.mail;

/**
 * 알림 메일 발송 — 구현을 바꿔 끼울 수 있게 분리 (SMTP / 로그 / 추후 메일 발송 서비스).
 * 실패하면 예외를 던진다.
 */
public interface NotificationMailSender {

	void send(String to, String subject, String html);

}
