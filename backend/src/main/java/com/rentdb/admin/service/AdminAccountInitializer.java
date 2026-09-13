package com.rentdb.admin.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.admin.AdminProperties;
import com.rentdb.admin.domain.AdminUser;
import com.rentdb.admin.repository.AdminUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 서버 시작 시 관리자 계정이 하나도 없으면 환경변수(INITIAL_ADMIN_EMAIL / INITIAL_ADMIN_PASSWORD)로 첫 계정을 만든다.
 * 비밀번호는 로그에 남기지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

	static final int MIN_PASSWORD_LENGTH = 8;

	private final AdminUserRepository adminUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final AdminProperties adminProperties;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (adminUserRepository.count() > 0) {
			return;
		}

		String email = adminProperties.initialEmail();
		String password = adminProperties.initialPassword();
		if (email == null || email.isBlank() || password == null || password.isBlank()) {
			log.warn("관리자 계정이 없습니다. .env(운영은 환경변수)에 INITIAL_ADMIN_EMAIL, INITIAL_ADMIN_PASSWORD 를 설정한 뒤 백엔드를 재기동하세요.");
			return;
		}
		if (password.length() < MIN_PASSWORD_LENGTH) {
			log.warn("INITIAL_ADMIN_PASSWORD 는 {}자 이상이어야 합니다. 첫 관리자 계정을 만들지 않았습니다.", MIN_PASSWORD_LENGTH);
			return;
		}

		String name = adminProperties.initialName() == null || adminProperties.initialName().isBlank()
				? "관리자" : adminProperties.initialName().strip();
		AdminUser admin = adminUserRepository.save(AdminUser.builder()
				.email(email)
				.passwordHash(passwordEncoder.encode(password))
				.name(name)
				.build());
		log.info("첫 관리자 계정을 만들었습니다: {}", admin.getEmail());
	}

}
