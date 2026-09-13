package com.rentdb.notification.mail;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.rentdb.notification.NotificationProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * SMTP 발송 — 로컬은 Mailpit(1025), 운영은 Gmail SMTP(587).
 */
@Component
@ConditionalOnProperty(prefix = "app.notification", name = "mail-type", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
public class SmtpNotificationMailSender implements NotificationMailSender {

	private final JavaMailSender javaMailSender;
	private final NotificationProperties properties;

	@Override
	public void send(String to, String subject, String html) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
			helper.setFrom(properties.from());
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(html, true);
			javaMailSender.send(message);
		}
		catch (MessagingException e) {
			throw new IllegalStateException("메일 메시지 생성 실패: " + e.getMessage(), e);
		}
	}

}
