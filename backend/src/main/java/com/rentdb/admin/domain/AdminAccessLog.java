package com.rentdb.admin.domain;

import java.time.OffsetDateTime;

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

/** 관리자 개인정보 접근·로그인 기록 (수정하지 않는 기록용 테이블) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admin_access_log")
public class AdminAccessLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	private AdminUser admin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccessAction action;

	@Column(name = "target_id")
	private Long targetId;

	@Column(name = "ip_hash", length = 64)
	private String ipHash;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Builder
	private AdminAccessLog(AdminUser admin, AccessAction action, Long targetId, String ipHash) {
		this.admin = admin;
		this.action = action;
		this.targetId = targetId;
		this.ipHash = ipHash;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}

}
