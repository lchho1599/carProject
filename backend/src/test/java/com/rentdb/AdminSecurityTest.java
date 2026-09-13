package com.rentdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

/**
 * 7단계 보안 필터 체인 통합 테스트 — 실제 로그인 필터·세션(DB)·CSRF 를 그대로 거친다.
 * 테스트용 관리자는 매번 임의 비밀번호로 만들고 끝나면 지운다.
 */
@SpringBootTest(properties = "app.security.login-rate-limit.max-requests=1000")
@AutoConfigureMockMvc
class AdminSecurityTest {

	private static final String EMAIL_DOMAIN = "@security.test";
	private static final String SESSION_COOKIE = "RENTDB_SESSION";

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	PasswordEncoder passwordEncoder;

	private Instant testStartedAt;
	private String email;
	private String password;

	@BeforeEach
	void setUp() {
		testStartedAt = Instant.now();
		email = "admin-" + UUID.randomUUID().toString().substring(0, 8) + EMAIL_DOMAIN;
		password = UUID.randomUUID().toString();
		jdbc.update("insert into admin_user (email, password_hash, name) values (?, ?, ?)",
				email, passwordEncoder.encode(password), "보안테스트");
	}

	@AfterEach
	void cleanUp() {
		jdbc.update("delete from admin_access_log where created_at >= ?", Timestamp.from(testStartedAt));
		jdbc.update("delete from spring_session where principal_name like ?", "%" + EMAIL_DOMAIN);
		jdbc.update("delete from admin_user where email like ?", "%" + EMAIL_DOMAIN);
	}

	// ---------------------------------------------------------------- 접근 제어

	@Test
	void 공개_API는_로그인과_CSRF_없이_사용할_수_있다() {
		assertThat(mvc.get().uri("/api/brands")).hasStatusOk();
		assertThat(mvc.get().uri("/actuator/health")).hasStatusOk();
		// 상담 신청은 CSRF 토큰 없이도 컨트롤러까지 도달한다 (빈 요청이라 400)
		assertThat(mvc.post().uri("/api/leads").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.hasStatus(400)
				.bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_FAILED");
	}

	@Test
	void 관리자_API는_로그인하지_않으면_401_JSON() {
		assertThat(mvc.get().uri("/api/admin/leads"))
				.hasStatus(401)
				.bodyJson().extractingPath("$.code").isEqualTo("UNAUTHORIZED");
		assertThat(mvc.get().uri("/api/admin/auth/me")).hasStatus(401);
	}

	@Test
	void 허용_목록에_없는_경로와_메서드는_차단한다() {
		assertThat(mvc.get().uri("/api/secret")).hasStatus(401);
		assertThat(mvc.get().uri("/actuator/env")).hasStatus(401);
		assertThat(mvc.delete().uri("/api/brands")).hasStatus(401);
		assertThat(mvc.get().uri("/anything-else")).hasStatus(401);
	}

	// ---------------------------------------------------------------- 로그인

	@Test
	void 비밀번호가_틀리면_401과_실패기록() {
		assertThat(login(email, "wrong-password"))
				.hasStatus(401)
				.bodyJson().extractingPath("$.code").isEqualTo("LOGIN_FAILED");

		assertThat(countAccessLogs("LOGIN_FAIL")).isEqualTo(1);
	}

	@Test
	void 없는_계정도_같은_메시지로_거부한다() {
		assertThat(login("nobody" + EMAIL_DOMAIN, password))
				.hasStatus(401)
				.bodyJson().extractingPath("$.message").isEqualTo("이메일 또는 비밀번호가 올바르지 않습니다.");
	}

	@Test
	void 로그인에_성공하면_보안_세션쿠키와_CSRF토큰을_받고_기록이_남는다() throws Exception {
		MvcTestResult result = login("  " + email.toUpperCase() + " ", password);

		assertThat(result).hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.email", v -> v.assertThat().isEqualTo(email))
				.hasPathSatisfying("$.name", v -> v.assertThat().isEqualTo("보안테스트"))
				.hasPathSatisfying("$.csrfToken", v -> v.assertThat().asString().isNotBlank());

		String setCookie = String.join(";", result.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
		assertThat(setCookie).contains(SESSION_COOKIE + "=", "HttpOnly", "SameSite=Lax");

		Cookie session = sessionCookie(result);
		assertThat(mvc.get().uri("/api/admin/auth/me").cookie(session))
				.hasStatusOk()
				.bodyJson().extractingPath("$.email").isEqualTo(email);

		assertThat(countAccessLogs("LOGIN")).isEqualTo(1);
		assertThat(jdbc.queryForObject("select last_login_at from admin_user where email = ?", Timestamp.class, email))
				.isNotNull();
		// 로그인 응답에는 비밀번호 해시 등 민감 정보가 없어야 한다
		assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).doesNotContain("password", "{bcrypt}");
	}

	@Test
	void 비활성_관리자는_로그인할_수_없다() {
		jdbc.update("update admin_user set is_active = false where email = ?", email);

		assertThat(login(email, password)).hasStatus(401);
	}

	@Test
	void 잘못된_형식의_로그인_요청은_401() {
		assertThat(mvc.post().uri("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content("not-json"))
				.hasStatus(401);
		assertThat(mvc.post().uri("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"\"}"))
				.hasStatus(401);
	}

	// ---------------------------------------------------------------- CSRF · 로그아웃

	@Test
	void 변경_요청은_CSRF_토큰이_있어야_한다() throws Exception {
		Long leadId = jdbc.queryForObject("select min(id) from lead", Long.class);
		String originalStatus = jdbc.queryForObject("select status from lead where id = ?", String.class, leadId);
		MvcTestResult loginResult = login(email, password);
		Cookie session = sessionCookie(loginResult);
		String csrfToken = JsonPath.read(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.csrfToken");

		try {
			assertThat(patchStatus(leadId, session, null))
					.hasStatus(403)
					.bodyJson().extractingPath("$.code").isEqualTo("CSRF_TOKEN_INVALID");
			assertThat(patchStatus(leadId, session, "wrong-token")).hasStatus(403);

			assertThat(patchStatus(leadId, session, csrfToken)).hasStatus(204);
			assertThat(jdbc.queryForObject("select status from lead where id = ?", String.class, leadId))
					.isEqualTo("IN_PROGRESS");
		}
		finally {
			jdbc.update("update lead set status = ? where id = ?", originalStatus, leadId);
		}
	}

	@Test
	void 로그아웃하면_세션이_무효화된다() throws Exception {
		MvcTestResult loginResult = login(email, password);
		Cookie session = sessionCookie(loginResult);
		String csrfToken = JsonPath.read(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.csrfToken");

		assertThat(mvc.post().uri("/api/admin/auth/logout").cookie(session).header("X-CSRF-TOKEN", csrfToken))
				.hasStatusOk();

		assertThat(mvc.get().uri("/api/admin/auth/me").cookie(session)).hasStatus(401);
	}

	@Test
	void 관리자_응답에는_보안_헤더가_붙는다() {
		MvcTestResult result = mvc.get().uri("/api/admin/auth/me").exchange();

		assertThat(result.getResponse().getHeader("X-Robots-Tag")).isEqualTo("noindex, nofollow");
		assertThat(result.getResponse().getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
		assertThat(result.getResponse().getHeader("Content-Security-Policy")).contains("frame-ancestors 'none'");
	}

	// ---------------------------------------------------------------- helpers

	private MvcTestResult login(String loginEmail, String loginPassword) {
		return mvc.post().uri("/api/admin/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(loginEmail, loginPassword))
				.exchange();
	}

	private MvcTestResult patchStatus(Long leadId, Cookie session, String csrfToken) {
		var request = mvc.patch().uri("/api/admin/leads/{id}", leadId)
				.cookie(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"IN_PROGRESS\"}");
		if (csrfToken != null) {
			request = request.header("X-CSRF-TOKEN", csrfToken);
		}
		return request.exchange();
	}

	private Cookie sessionCookie(MvcTestResult result) {
		Cookie cookie = result.getResponse().getCookie(SESSION_COOKIE);
		assertThat(cookie).as("세션 쿠키").isNotNull();
		return cookie;
	}

	private long countAccessLogs(String action) {
		return jdbc.queryForObject("select count(*) from admin_access_log where action = ? and created_at >= ?",
				Long.class, action, Timestamp.from(testStartedAt));
	}

}
