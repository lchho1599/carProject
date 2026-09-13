package com.rentdb.global.health;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프론트엔드 ↔ 백엔드 연결 확인용 API.
 */
@RestController
@RequestMapping("/api")
public class PingController {

	@GetMapping("/ping")
	public Map<String, Object> ping() {
		return Map.of(
				"status", "ok",
				"serverTime", OffsetDateTime.now().toString());
	}

}
