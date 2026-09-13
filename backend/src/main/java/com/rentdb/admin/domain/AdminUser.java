package com.rentdb.admin.domain;

import java.time.OffsetDateTime;

import com.rentdb.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 관리자 계정 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admin_user")
public class AdminUser extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String email;

	/** BCrypt 해시 */
	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "last_login_at")
	private OffsetDateTime lastLoginAt;

	@Builder
	private AdminUser(String email, String passwordHash, String name) {
		this.email = normalizeEmail(email);
		this.passwordHash = passwordHash;
		this.name = name;
	}

	/** 이메일은 소문자·앞뒤 공백 제거 형태로 저장하고 조회한다 */
	public static String normalizeEmail(String email) {
		return email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT);
	}

	public void recordLogin(OffsetDateTime loggedInAt) {
		this.lastLoginAt = loggedInAt;
	}

	public void deactivate() {
		this.active = false;
	}

	public void update(String name, boolean active) {
		this.name = name;
		this.active = active;
	}

	/** passwordHash 는 PasswordEncoder 로 만든 해시 */
	public void changePassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}

}
