package com.rentdb.notification.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rentdb.global.config.AsyncConfig;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.event.LeadCreatedEvent;
import com.rentdb.lead.repository.LeadRepository;
import com.rentdb.notification.domain.NotificationChannel;
import com.rentdb.notification.domain.NotificationLog;
import com.rentdb.notification.domain.NotificationStatus;
import com.rentdb.notification.mail.NotificationMailSender;
import com.rentdb.notification.repository.NotificationLogRepository;
import com.rentdb.setting.service.AppSettingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 신규 상담 알림 — 신청 트랜잭션이 커밋된 뒤 별도 스레드에서 발송한다.
 * 발송이 실패해도 신청은 이미 저장되어 있으며, 결과는 notification_log에 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadNotificationService {

	private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

	private final LeadRepository leadRepository;
	private final AppSettingService appSettingService;
	private final LeadMailTemplate mailTemplate;
	private final NotificationMailSender mailSender;
	private final NotificationLogRepository notificationLogRepository;

	@Async(AsyncConfig.NOTIFICATION_EXECUTOR)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onLeadCreated(LeadCreatedEvent event) {
		notifyNewLead(event.leadId());
	}

	public void notifyNewLead(Long leadId) {
		Lead lead = leadRepository.findById(leadId).orElse(null);
		if (lead == null) {
			log.warn("알림 대상 리드를 찾을 수 없습니다. leadId={}", leadId);
			return;
		}

		List<String> recipients = appSettingService.getNotifyEmails();
		if (recipients.isEmpty()) {
			log.warn("알림 수신 메일이 설정되지 않아 발송하지 않습니다. leadId={} (관리자 > 설정에서 등록)", leadId);
			return;
		}

		LeadMailTemplate.MailContent content = mailTemplate.render(lead);
		for (String recipient : recipients) {
			try {
				mailSender.send(recipient, content.subject(), content.html());
				saveLog(lead, recipient, NotificationStatus.SENT, null);
			}
			catch (RuntimeException e) {
				log.error("알림 메일 발송 실패. leadId={}, recipient={}", leadId, recipient, e);
				saveLog(lead, recipient, NotificationStatus.FAILED, e.getMessage());
			}
		}
	}

	private void saveLog(Lead lead, String recipient, NotificationStatus status, String errorMessage) {
		if (errorMessage != null && errorMessage.length() > ERROR_MESSAGE_MAX_LENGTH) {
			errorMessage = errorMessage.substring(0, ERROR_MESSAGE_MAX_LENGTH);
		}
		notificationLogRepository.save(NotificationLog.builder()
				.lead(lead)
				.channel(NotificationChannel.EMAIL)
				.recipient(recipient)
				.status(status)
				.errorMessage(errorMessage)
				.build());
	}

}
