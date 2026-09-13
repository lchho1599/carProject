package com.rentdb.lead.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeadNoteCreateRequest(
		@NotBlank(message = "메모 내용을 입력해 주세요.")
		@Size(max = 2000, message = "메모는 2000자 이내로 입력해 주세요.")
		String content) {
}
