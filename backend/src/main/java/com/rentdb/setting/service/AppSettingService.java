package com.rentdb.setting.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.setting.domain.AppSetting;
import com.rentdb.setting.repository.AppSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppSettingService {

	private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
	};

	private final AppSettingRepository appSettingRepository;
	private final JsonMapper jsonMapper;

	/** 알림 수신 메일 목록 — 설정이 없거나 형식이 잘못되면 빈 목록 */
	public List<String> getNotifyEmails() {
		return appSettingRepository.findById(AppSetting.NOTIFY_EMAILS)
				.map(setting -> {
					try {
						return jsonMapper.readValue(setting.getValue(), STRING_LIST).stream()
								.filter(email -> email != null && !email.isBlank())
								.map(String::strip)
								.toList();
					}
					catch (RuntimeException e) {
						log.error("notify_emails 설정 형식이 올바르지 않습니다: {}", setting.getValue(), e);
						return List.<String>of();
					}
				})
				.orElse(List.of());
	}

}
