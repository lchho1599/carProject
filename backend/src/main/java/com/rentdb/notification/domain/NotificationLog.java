package com.rentdb.notification.domain;

import java.time.OffsetDateTime;

import com.rentdb.lead.domain.Lead;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 발송 결과 기록 (수정하지 않는 기록용 테이블) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notification_log")
public class NotificationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lead_id")
	private Lead lead;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationChannel channel;

	@Column(nullable = false, length = 200)
	private String recipient;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationStatus status;

	@Column(name = "error_message", length = 1000)
	private String errorMessage;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Builder
	private NotificationLog(Lead lead, NotificationChannel channel, String recipient, NotificationStatus status,
			String errorMessage) {
		this.lead = lead;
		this.channel = channel;
		this.recipient = recipient;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}

}
