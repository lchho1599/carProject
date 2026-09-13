package com.rentdb.admin.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.admin.domain.AccessAction;
import com.rentdb.admin.domain.AdminUser;
import com.rentdb.admin.dto.AdminAccountDtos.AccountResponse;
import com.rentdb.admin.dto.AdminAccountDtos.CreateRequest;
import com.rentdb.admin.dto.AdminAccountDtos.UpdateRequest;
import com.rentdb.admin.repository.AdminUserRepository;
import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 계정 관리.
 * - 본인 계정은 비활성화할 수 없다 (로그인한 관리자가 곧 활성 관리자이므로, 이 규칙으로 "활성 관리자 0명" 상태를 막는다)
 * - 비활성화·비밀번호 재설정 시 대상 관리자의 세션을 즉시 끊는다
 * - 모든 변경은 접근 기록(admin_access_log)에 남긴다
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountService {

	private final AdminUserRepository adminUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final AdminSessionService sessionService;
	private final AdminAccessLogService accessLogService;

	public List<AccountResponse> getAccounts(Long currentAdminId) {
		return adminUserRepository.findAll(Sort.by("id")).stream()
				.map(admin -> AccountResponse.of(admin, currentAdminId))
				.toList();
	}

	@Transactional
	public AccountResponse create(CreateRequest request, Long currentAdminId, HttpServletRequest httpRequest) {
		String email = AdminUser.normalizeEmail(request.email());
		if (adminUserRepository.findByEmail(email).isPresent()) {
			throw new BusinessException(ErrorCode.DUPLICATED, "이미 등록된 이메일입니다.");
		}
		PasswordPolicy.validate(request.password());

		AdminUser admin = adminUserRepository.save(AdminUser.builder()
				.email(email)
				.name(request.name().strip())
				.passwordHash(passwordEncoder.encode(request.password()))
				.build());
		accessLogService.record(currentAdminId, AccessAction.ADMIN_CREATE, admin.getId(), httpRequest);
		return AccountResponse.of(admin, currentAdminId);
	}

	@Transactional
	public AccountResponse update(Long adminId, UpdateRequest request, Long currentAdminId,
			HttpServletRequest httpRequest) {
		AdminUser admin = findAdmin(adminId);
		boolean deactivating = admin.isActive() && !request.active();
		if (deactivating && adminId.equals(currentAdminId)) {
			throw new BusinessException(ErrorCode.VALIDATION_FAILED, "본인 계정은 비활성화할 수 없습니다.");
		}

		admin.update(request.name().strip(), request.active());
		if (deactivating) {
			sessionService.invalidateAll(admin.getEmail());
		}
		accessLogService.record(currentAdminId, AccessAction.ADMIN_UPDATE, adminId, httpRequest);
		return AccountResponse.of(admin, currentAdminId);
	}

	/** 다른 관리자의 비밀번호 재설정 — 본인은 changeMyPassword 사용 */
	@Transactional
	public void resetPassword(Long adminId, String newPassword, Long currentAdminId, HttpServletRequest httpRequest) {
		if (adminId.equals(currentAdminId)) {
			throw new BusinessException(ErrorCode.VALIDATION_FAILED, "본인 비밀번호는 '내 비밀번호 변경'에서 바꿔 주세요.");
		}
		AdminUser admin = findAdmin(adminId);
		PasswordPolicy.validate(newPassword);

		admin.changePassword(passwordEncoder.encode(newPassword));
		sessionService.invalidateAll(admin.getEmail());
		accessLogService.record(currentAdminId, AccessAction.PASSWORD_RESET, adminId, httpRequest);
	}

	/** 본인 비밀번호 변경 — 지금 쓰는 세션은 유지하고 다른 기기의 세션은 끊는다 */
	@Transactional
	public void changeMyPassword(Long currentAdminId, String currentPassword, String newPassword,
			HttpServletRequest httpRequest) {
		AdminUser admin = findAdmin(currentAdminId);
		if (!passwordEncoder.matches(currentPassword, admin.getPasswordHash())) {
			throw new BusinessException(ErrorCode.VALIDATION_FAILED, "현재 비밀번호가 올바르지 않습니다.");
		}
		if (passwordEncoder.matches(newPassword, admin.getPasswordHash())) {
			throw new BusinessException(ErrorCode.VALIDATION_FAILED, "새 비밀번호가 현재 비밀번호와 같습니다.");
		}
		PasswordPolicy.validate(newPassword);

		admin.changePassword(passwordEncoder.encode(newPassword));
		HttpSession session = httpRequest.getSession(false);
		sessionService.invalidateAllExcept(admin.getEmail(), session == null ? null : session.getId());
		accessLogService.record(currentAdminId, AccessAction.PASSWORD_CHANGE, currentAdminId, httpRequest);
	}

	private AdminUser findAdmin(Long adminId) {
		return adminUserRepository.findById(adminId).orElseThrow(() -> BusinessException.notFound("관리자"));
	}

}
