package com.rentdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** 로그인 시도 제한 필터 — 한도를 2회로 낮춰 3번째 시도가 429 로 막히는지 확인 */
@SpringBootTest(properties = "app.security.login-rate-limit.max-requests=2")
@AutoConfigureMockMvc
class AdminLoginRateLimitTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JdbcTemplate jdbc;

	private final Instant startedAt = Instant.now();

	@AfterEach
	void cleanUp() {
		jdbc.update("delete from admin_access_log where created_at >= ?", Timestamp.from(startedAt));
	}

	@Test
	void 같은_IP의_로그인_시도가_한도를_넘으면_429() {
		for (int i = 0; i < 2; i++) {
			assertThat(attempt("203.0.113.7")).hasStatus(401);
		}

		assertThat(attempt("203.0.113.7"))
				.hasStatus(429)
				.bodyJson().extractingPath("$.code").isEqualTo("TOO_MANY_REQUESTS");

		assertThat(attempt("198.51.100.20")).as("다른 IP는 영향 없음").hasStatus(401);
	}

	private org.springframework.test.web.servlet.assertj.MvcTestResult attempt(String ip) {
		return mvc.post().uri("/api/admin/auth/login")
				.header("CF-Connecting-IP", ip)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"nobody@ratelimit.test\",\"password\":\"wrong-password\"}")
				.exchange();
	}

}
