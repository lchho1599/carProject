package com.rentdb.setting.domain;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.rentdb.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 키-값 설정 (예: notify_emails = ["ops@example.com"]) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "app_setting")
public class AppSetting extends BaseTimeEntity {

	public static final String NOTIFY_EMAILS = "notify_emails";

	@Id
	@Column(name = "setting_key", length = 100)
	private String key;

	/** JSON 문자열 그대로 저장 */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private String value;

	public AppSetting(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public void changeValue(String value) {
		this.value = value;
	}

}
