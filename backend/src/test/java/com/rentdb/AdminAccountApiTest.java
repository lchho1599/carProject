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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

/**
 * 관리자 계정 관리 통합 테스트 — 실제 로그인 필터·세션(DB)·CSRF 를 거친다.
 * 테스트 관리자는 매번 임의 비밀번호로 만들고 끝나면 지운다.
 */
@SpringBootTest(properties = "app.security.login-rate-limit.max-requests=1000")
@AutoConfigureMockMvc
class AdminAccountApiTest {

	private static final String DOMAIN = "@account.test";

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	PasswordEncoder passwordEncoder;

	private Instant startedAt;

	/** 로그인한 관리자 A (작업 수행자) */
	private Login actor;

	private record Login(Long id, String email, String password, Cookie session, String csrfToken) {
	}

	@BeforeEach
	void setUp() throws Exception {
		startedAt = Instant.now();
		actor = createAndLogin("actor");
	}

	@AfterEach
	void cleanUp() {
		jdbc.update("delete from admin_access_log where created_at >= ?", Timestamp.from(startedAt));
		jdbc.update("delete from spring_session where principal_name like ?", "%" + DOMAIN);
		jdbc.update("delete from admin_user where email like ?", "%" + DOMAIN);
	}

	// ---------------------------------------------------------------- 목록·추가

	@Test
	void 목록은_비밀번호_정보없이_본인을_표시한다() throws Exception {
		MvcTestResult result = mvc.get().uri("/api/admin/users").cookie(actor.session()).exchange();

		assertThat(result).hasStatusOk();
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(body).doesNotContain("password", "{bcrypt}");
		// 조건 검색 결과는 목록으로 온다
		java.util.List<Boolean> meFlags = JsonPath.read(body, "$[?(@.id == %d)].me".formatted(actor.id()));
		assertThat(meFlags).containsExactly(true);
		java.util.List<Boolean> otherFlags = JsonPath.read(body, "$[?(@.id != %d)].me".formatted(actor.id()));
		assertThat(otherFlags).doesNotContain(true);
	}

	@Test
	void 관리자를_추가하면_그_계정으로_로그인할_수_있고_기록이_남는다() throws Exception {
		String email = "NewAdmin-" + shortId() + DOMAIN;
		String password = "pw" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "7";

		MvcTestResult created = send("POST", "/api/admin/users",
				"{\"email\":\"%s\",\"name\":\"신규관리자\",\"password\":\"%s\"}".formatted(email, password));

		assertThat(created).hasStatus(201)
				.bodyJson()
				.hasPathSatisfying("$.email", v -> v.assertThat().isEqualTo(email.toLowerCase()))
				.hasPathSatisfying("$.active", v -> v.assertThat().isEqualTo(true));
		assertThat(login(email.toLowerCase(), password)).hasStatusOk();
		assertThat(countLogs("ADMIN_CREATE")).isEqualTo(1);
	}

	@Test
	void 이메일_중복과_규칙에_맞지_않는_비밀번호는_거부한다() {
		assertThat(send("POST", "/api/admin/users",
				"{\"email\":\"%s\",\"name\":\"중복\",\"password\":\"abcd1234\"}".formatted(actor.email().toUpperCase())))
				.hasStatus(409)
				.bodyJson().extractingPath("$.code").isEqualTo("DUPLICATED");

		for (String weak : new String[] { "short1", "onlyletters", "12345678", " spaced12 " }) {
			assertThat(send("POST", "/api/admin/users",
					"{\"email\":\"weak-%s%s\",\"name\":\"약한\",\"password\":\"%s\"}".formatted(shortId(), DOMAIN, weak)))
					.as(weak).hasStatus(400)
					.bodyJson().extractingPath("$.message").asString().contains("영문과 숫자");
		}
	}

	// ---------------------------------------------------------------- 비활성화

	@Test
	void 다른_관리자를_비활성화하면_그_세션이_끊기고_다시_로그인할_수_없다() throws Exception {
		Login target = createAndLogin("target");
		assertThat(mvc.get().uri("/api/admin/auth/me").cookie(target.session())).hasStatusOk();

		assertThat(send("PUT", "/api/admin/users/" + target.id(), "{\"name\":\"대상\",\"active\":false}"))
				.hasStatusOk()
				.bodyJson().extractingPath("$.active").isEqualTo(false);

		assertThat(mvc.get().uri("/api/admin/auth/me").cookie(target.session())).as("기존 세션 끊김").hasStatus(401);
		assertThat(login(target.email(), target.password())).as("재로그인 불가").hasStatus(401);
		assertThat(countLogs("ADMIN_UPDATE")).isEqualTo(1);

		// 다시 활성화하면 로그인 가능
		assertThat(send("PUT", "/api/admin/users/" + target.id(), "{\"name\":\"대상\",\"active\":true}")).hasStatusOk();
		assertThat(login(target.email(), target.password())).hasStatusOk();
	}

	@Test
	void 본인_계정은_비활성화할_수_없지만_이름은_바꿀_수_있다() {
		assertThat(send("PUT", "/api/admin/users/" + actor.id(), "{\"name\":\"작업자\",\"active\":false}"))
				.hasStatus(400)
				.bodyJson().extractingPath("$.message").isEqualTo("본인 계정은 비활성화할 수 없습니다.");

		assertThat(send("PUT", "/api/admin/users/" + actor.id(), "{\"name\":\"새이름\",\"active\":true}"))
				.hasStatusOk()
				.bodyJson().extractingPath("$.name").isEqualTo("새이름");
	}

	// ---------------------------------------------------------------- 비밀번호

	@Test
	void 다른_관리자_비밀번호를_재설정하면_세션이_끊기고_새_비밀번호로만_로그인된다() throws Exception {
		Login target = createAndLogin("reset");
		String newPassword = "reset" + shortId() + "9";

		assertThat(send("PUT", "/api/admin/users/" + target.id() + "/password",
				"{\"newPassword\":\"%s\"}".formatted(newPassword))).hasStatus(204);

		assertThat(mvc.get().uri("/api/admin/auth/me").cookie(target.session())).hasStatus(401);
		assertThat(login(target.email(), target.password())).as("이전 비밀번호").hasStatus(401);
		assertThat(login(target.email(), newPassword)).as("새 비밀번호").hasStatusOk();
		assertThat(countLogs("PASSWORD_RESET")).isEqualTo(1);

		assertThat(send("PUT", "/api/admin/users/" + actor.id() + "/password", "{\"newPassword\":\"abcd12345\"}"))
				.as("본인은 재설정 대신 내 비밀번호 변경").hasStatus(400);
	}

	@Test
	void 내_비밀번호_변경은_현재_비밀번호를_확인하고_지금_세션은_유지하며_다른_세션은_끊는다() throws Exception {
		Login otherDevice = loginAs(actor.id(), actor.email(), actor.password());
		String newPassword = "mine" + shortId() + "5";

		assertThat(send("PUT", "/api/admin/users/me/password",
				"{\"currentPassword\":\"wrong-pass1\",\"newPassword\":\"%s\"}".formatted(newPassword)))
				.hasStatus(400)
				.bodyJson().extractingPath("$.message").isEqualTo("현재 비밀번호가 올바르지 않습니다.");
		assertThat(send("PUT", "/api/admin/users/me/password",
				"{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}".formatted(actor.password(), actor.password())))
				.hasStatus(400);

		assertThat(send("PUT", "/api/admin/users/me/password",
				"{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}".formatted(actor.password(), newPassword)))
				.hasStatus(204);

		assertThat(mvc.get().uri("/api/admin/auth/me").cookie(actor.session())).as("지금 세션 유지").hasStatusOk();
		assertThat(mvc.get().uri("/api/admin/auth/me").cookie(otherDevice.session())).as("다른 기기 세션 끊김").hasStatus(401);
		assertThat(login(actor.email(), newPassword)).hasStatusOk();
		assertThat(countLogs("PASSWORD_CHANGE")).isEqualTo(1);
	}

	@Test
	void 계정_관리_API는_로그인과_CSRF_토큰이_필요하다() {
		assertThat(mvc.get().uri("/api/admin/users")).hasStatus(401);
		assertThat(mvc.post().uri("/api/admin/users").cookie(actor.session())
				.contentType(MediaType.APPLICATION_JSON).content("{}")).as("CSRF 없음").hasStatus(403);
	}

	// ---------------------------------------------------------------- helpers

	private Login createAndLogin(String prefix) throws Exception {
		String email = prefix + "-" + shortId() + DOMAIN;
		String password = "pw" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "1";
		Long id = jdbc.queryForObject("insert into admin_user (email, password_hash, name) values (?, ?, ?) returning id",
				Long.class, email, passwordEncoder.encode(password), prefix);
		return loginAs(id, email, password);
	}

	private Login loginAs(Long id, String email, String password) throws Exception {
		MvcTestResult result = login(email, password);
		assertThat(result).hasStatusOk();
		Cookie session = result.getResponse().getCookie("RENTDB_SESSION");
		String csrf = JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.csrfToken");
		return new Login(id, email, password, session, csrf);
	}

	private MvcTestResult login(String email, String password) {
		return mvc.post().uri("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)).exchange();
	}

	private MvcTestResult send(String method, String uri, String json) {
		var builder = switch (method) {
			case "POST" -> mvc.post();
			case "PUT" -> mvc.put();
			default -> throw new IllegalArgumentException(method);
		};
		return builder.uri(uri).cookie(actor.session()).header("X-CSRF-TOKEN", actor.csrfToken())
				.contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private long countLogs(String action) {
		return jdbc.queryForObject("select count(*) from admin_access_log where action = ? and created_at >= ?",
				Long.class, action, Timestamp.from(startedAt));
	}

	private static String shortId() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

}
