package com.rentdb.global.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.admin.repository.AdminUserRepository;

import lombok.RequiredArgsConstructor;

/** 로그인 시 이메일로 관리자 계정을 찾는다 (비활성 계정은 isEnabled=false 로 거부됨) */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

	private final AdminUserRepository adminUserRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) {
		String normalized = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
		return adminUserRepository.findByEmail(normalized)
				.map(AdminPrincipal::from)
				.orElseThrow(() -> new UsernameNotFoundException("관리자 계정 없음"));
	}

}
