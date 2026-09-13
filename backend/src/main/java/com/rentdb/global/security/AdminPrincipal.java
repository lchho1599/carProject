package com.rentdb.global.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rentdb.admin.domain.AdminUser;

import lombok.Getter;

/**
 * 로그인한 관리자 — 세션(DB)에 직렬화되어 저장된다.
 * 인증이 끝나면 비밀번호 해시를 지워(CredentialsContainer) 세션에 남기지 않는다.
 */
@Getter
public class AdminPrincipal implements UserDetails, CredentialsContainer {

	public static final String ROLE_ADMIN = "ADMIN";

	private final Long id;
	private final String email;
	private final String name;
	private final boolean active;
	private String passwordHash;

	private AdminPrincipal(Long id, String email, String name, boolean active, String passwordHash) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.active = active;
		this.passwordHash = passwordHash;
	}

	public static AdminPrincipal from(AdminUser admin) {
		return new AdminPrincipal(admin.getId(), admin.getEmail(), admin.getName(), admin.isActive(),
				admin.getPasswordHash());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + ROLE_ADMIN));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}

	@Override
	public void eraseCredentials() {
		this.passwordHash = null;
	}

}
