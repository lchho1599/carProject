package com.rentdb.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.admin.domain.AccessAction;
import com.rentdb.admin.domain.AdminAccessLog;
import com.rentdb.admin.repository.AdminAccessLogRepository;
import com.rentdb.admin.repository.AdminUserRepository;
import com.rentdb.global.web.ClientInfoResolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 로그인·개인정보 조회 기록 (설계 문서 §4).
 * 기록 저장이 본 작업의 롤백에 휩쓸리지 않도록 별도 트랜잭션으로 저장한다.
 */
@Service
@RequiredArgsConstructor
public class AdminAccessLogService {

	private final AdminAccessLogRepository accessLogRepository;
	private final AdminUserRepository adminUserRepository;
	private final ClientInfoResolver clientInfoResolver;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void record(Long adminId, AccessAction action, Long targetId, HttpServletRequest request) {
		accessLogRepository.save(AdminAccessLog.builder()
				.admin(adminId == null ? null : adminUserRepository.getReferenceById(adminId))
				.action(action)
				.targetId(targetId)
				.ipHash(clientInfoResolver.resolve(request).ipHash())
				.build());
	}

}
