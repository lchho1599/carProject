package com.rentdb.admin.dto;

import com.rentdb.global.security.AdminPrincipal;

/**
 * 로그인한 관리자 정보 + CSRF 토큰.
 * 관리자 화면은 이 토큰을 메모리에 두고 변경 요청(POST/PATCH/PUT/DELETE)마다 X-CSRF-TOKEN 헤더로 보낸다.
 */
public record AdminMeResponse(Long id, String email, String name, String csrfToken) {

	public static AdminMeResponse of(AdminPrincipal principal, String csrfToken) {
		return new AdminMeResponse(principal.getId(), principal.getEmail(), principal.getName(), csrfToken);
	}

}
