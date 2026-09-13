package com.rentdb.global.config;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 현재 시각은 Clock으로 주입받는다 (노출 기간 판단 등을 테스트에서 고정할 수 있게).
 */
@Configuration
public class TimeConfig {

	public static final ZoneId KST = ZoneId.of("Asia/Seoul");

	@Bean
	public Clock clock() {
		return Clock.system(KST);
	}

	/**
	 * DB에서 읽은 시각은 UTC 오프셋으로 나오므로, API 응답에서는 한국 시간(+09:00)으로 바꿔 내려준다.
	 */
	public static OffsetDateTime toKst(OffsetDateTime dateTime) {
		return dateTime == null ? null : dateTime.atZoneSameInstant(KST).toOffsetDateTime();
	}

}
