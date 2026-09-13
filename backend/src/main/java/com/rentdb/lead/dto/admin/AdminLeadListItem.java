package com.rentdb.lead.dto.admin;

import java.time.OffsetDateTime;

import com.rentdb.global.common.Masking;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.domain.LeadStatus;
import com.rentdb.lead.domain.LeadType;
import com.rentdb.lead.support.LeadLabels;

/** 관리자 리드 목록 한 줄 — 이름·연락처는 마스킹 (전체는 상세에서만) */
public record AdminLeadListItem(
		Long id,
		LeadType type,
		LeadStatus status,
		String maskedName,
		String maskedPhone,
		String vehicleName,
		Long totalPrice,
		OffsetDateTime createdAt) {

	public static AdminLeadListItem from(Lead lead) {
		return new AdminLeadListItem(
				lead.getId(),
				lead.getType(),
				lead.getStatus(),
				Masking.name(lead.getName()),
				Masking.phone(lead.getPhone()),
				LeadLabels.vehicleName(lead.getVehicleSnapshot()),
				lead.getTotalPrice(),
				TimeConfig.toKst(lead.getCreatedAt()));
	}

}
