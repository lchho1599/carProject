package com.rentdb.lead.dto.admin;

import com.rentdb.lead.domain.LeadStatus;

import jakarta.validation.constraints.NotNull;

public record LeadStatusUpdateRequest(@NotNull(message = "변경할 상태를 선택해 주세요.") LeadStatus status) {
}
