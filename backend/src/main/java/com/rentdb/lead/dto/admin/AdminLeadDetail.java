package com.rentdb.lead.dto.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.rentdb.global.config.TimeConfig;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.domain.LeadNote;
import com.rentdb.lead.domain.LeadStatus;
import com.rentdb.lead.domain.LeadType;
import com.rentdb.lead.support.LeadLabels;
import com.rentdb.notification.domain.NotificationLog;
import com.rentdb.notification.domain.NotificationStatus;

/** 관리자 리드 상세 — 이름·연락처 전체 포함 (조회 시 LEAD_VIEW 기록) */
public record AdminLeadDetail(
		Long id,
		LeadType type,
		LeadStatus status,
		String name,
		String phone,
		String vehicleName,
		Map<String, Object> vehicleSnapshot,
		Map<String, Object> conditions,
		String conditionSummary,
		Long totalPrice,
		Long dealId,
		Long instantStockId,
		boolean agreePrivacy,
		boolean agreeMarketing,
		OffsetDateTime agreedAt,
		String sourceUrl,
		Map<String, String> utm,
		String userAgent,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		List<Note> notes,
		List<Notification> notifications) {

	public record Note(Long id, String content, String adminName, OffsetDateTime createdAt) {
		static Note from(LeadNote note) {
			return new Note(note.getId(), note.getContent(),
					note.getAdmin() == null ? "(삭제된 관리자)" : note.getAdmin().getName(),
					TimeConfig.toKst(note.getCreatedAt()));
		}
	}

	public record Notification(String recipient, NotificationStatus status, OffsetDateTime createdAt) {
		static Notification from(NotificationLog log) {
			return new Notification(log.getRecipient(), log.getStatus(), TimeConfig.toKst(log.getCreatedAt()));
		}
	}

	public static AdminLeadDetail of(Lead lead, List<LeadNote> notes, List<NotificationLog> notifications) {
		return new AdminLeadDetail(
				lead.getId(),
				lead.getType(),
				lead.getStatus(),
				lead.getName(),
				LeadLabels.formatPhone(lead.getPhone()),
				LeadLabels.vehicleName(lead.getVehicleSnapshot()),
				lead.getVehicleSnapshot(),
				lead.getConditions(),
				LeadLabels.conditionSummary(lead.getConditions()),
				lead.getTotalPrice(),
				lead.getDeal() == null ? null : lead.getDeal().getId(),
				lead.getInstantStock() == null ? null : lead.getInstantStock().getId(),
				lead.isAgreePrivacy(),
				lead.isAgreeMarketing(),
				TimeConfig.toKst(lead.getAgreedAt()),
				lead.getSourceUrl(),
				lead.getUtm(),
				lead.getUserAgent(),
				TimeConfig.toKst(lead.getCreatedAt()),
				TimeConfig.toKst(lead.getUpdatedAt()),
				notes.stream().map(Note::from).toList(),
				notifications.stream().map(Notification::from).toList());
	}

}
