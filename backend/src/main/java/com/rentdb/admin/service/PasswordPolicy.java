package com.rentdb.admin.service;

import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;

/**
 * 관리자 비밀번호 규칙 — 8~100자, 영문과 숫자를 모두 포함.
 * (첫 관리자 생성용 AdminAccountInitializer 는 길이만 확인한다)
 */
public final class PasswordPolicy {

	public static final int MIN_LENGTH = 8;
	public static final int MAX_LENGTH = 100;
	public static final String DESCRIPTION = "비밀번호는 8~100자이며 영문과 숫자를 모두 포함해야 합니다.";

	private PasswordPolicy() {
	}

	public static void validate(String password) {
		if (password == null
				|| password.length() < MIN_LENGTH
				|| password.length() > MAX_LENGTH
				|| !password.chars().anyMatch(Character::isLetter)
				|| !password.chars().anyMatch(Character::isDigit)
				|| !password.equals(password.strip())) {
			throw new BusinessException(ErrorCode.VALIDATION_FAILED, DESCRIPTION);
		}
	}

}
