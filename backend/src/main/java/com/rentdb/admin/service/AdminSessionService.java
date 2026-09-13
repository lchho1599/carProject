package com.rentdb.admin.service;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 로그인 세션 강제 종료 — Spring Session JDBC 는 세션마다 로그인 이메일(principal name)을 저장하므로
 * 이메일로 찾아 삭제할 수 있다.
 */
@Service
@RequiredArgsConstructor
public class AdminSessionService {

	private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

	/** 해당 관리자의 모든 세션을 끊는다 (비활성화·비밀번호 재설정) */
	public int invalidateAll(String email) {
		return invalidateAllExcept(email, null);
	}

	/** 지금 사용 중인 세션(keepSessionId)만 남기고 끊는다 (본인 비밀번호 변경) */
	public int invalidateAllExcept(String email, String keepSessionId) {
		var sessions = sessionRepository.findByPrincipalName(email);
		int count = 0;
		for (String sessionId : sessions.keySet()) {
			if (!sessionId.equals(keepSessionId)) {
				sessionRepository.deleteById(sessionId);
				count++;
			}
		}
		return count;
	}

}
