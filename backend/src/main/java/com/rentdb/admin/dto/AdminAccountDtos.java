package com.rentdb.admin.dto;

import java.time.OffsetDateTime;

import com.rentdb.admin.domain.AdminUser;
import com.rentdb.global.config.TimeConfig;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 관리자 계정 관리 요청·응답 — 응답에는 비밀번호 해시를 절대 포함하지 않는다 */
public final class AdminAccountDtos {

	private AdminAccountDtos() {
	}

	public record AccountResponse(
			Long id,
			String email,
			String name,
			boolean active,
			OffsetDateTime lastLoginAt,
			OffsetDateTime createdAt,
			/** 로그인한 본인 계정인지 */
			boolean me) {

		public static AccountResponse of(AdminUser admin, Long currentAdminId) {
			return new AccountResponse(admin.getId(), admin.getEmail(), admin.getName(), admin.isActive(),
					TimeConfig.toKst(admin.getLastLoginAt()), TimeConfig.toKst(admin.getCreatedAt()),
					admin.getId().equals(currentAdminId));
		}
	}

	public record CreateRequest(
			@NotBlank(message = "이메일을 입력해 주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") @Size(max = 200) String email,
			@NotBlank(message = "이름을 입력해 주세요.") @Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.") String name,
			@NotBlank(message = "초기 비밀번호를 입력해 주세요.") String password) {
	}

	public record UpdateRequest(
			@NotBlank(message = "이름을 입력해 주세요.") @Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.") String name,
			@NotNull(message = "사용 여부를 선택해 주세요.") Boolean active) {
	}

	public record PasswordChangeRequest(
			@NotBlank(message = "현재 비밀번호를 입력해 주세요.") String currentPassword,
			@NotBlank(message = "새 비밀번호를 입력해 주세요.") String newPassword) {
	}

	public record PasswordResetRequest(
			@NotBlank(message = "새 비밀번호를 입력해 주세요.") String newPassword) {
	}

}
