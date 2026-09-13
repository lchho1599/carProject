package com.rentdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.rentdb.admin.domain.AdminUser;
import com.rentdb.admin.repository.AdminUserRepository;
import com.rentdb.global.security.AdminPrincipal;

/**
 * 7단계 관리자 리드 관리 API 테스트.
 * 로그인 자체는 AdminSecurityTest 에서 검증하고, 여기서는 로그인된 관리자(user())로 기능을 확인한다.
 * 조회 기록이 별도 트랜잭션으로 저장되므로 롤백 대신 테스트 데이터(연락처 0108…)를 끝나고 지운다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminLeadApiTest {

	private static final String PHONE_PREFIX = "0108";

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	AdminUserRepository adminUserRepository;

	private Instant startedAt;
	private AdminPrincipal admin;

	@BeforeEach
	void setUp() {
		startedAt = Instant.now();
		AdminUser saved = adminUserRepository.save(AdminUser.builder()
				.email("lead-admin-" + UUID.randomUUID().toString().substring(0, 8) + "@lead.test")
				.passwordHash("{noop}unused")
				.name("김관리")
				.build());
		admin = AdminPrincipal.from(saved);
	}

	@AfterEach
	void cleanUp() {
		jdbc.update("delete from lead where phone like ?", PHONE_PREFIX + "%");
		jdbc.update("delete from admin_access_log where created_at >= ?", Timestamp.from(startedAt));
		jdbc.update("delete from admin_user where email like '%@lead.test'");
	}

	// ---------------------------------------------------------------- 목록

	@Test
	void 목록은_이름과_연락처를_가리고_최신순으로_준다() {
		insertLead("홍길동", "01080001111", "QUICK", "NEW", "2030-01-10 10:00:00+09");
		insertLead("김철수", "01080002222", "ESTIMATE", "IN_PROGRESS", "2030-01-11 09:00:00+09");

		assertThat(get("/api/admin/leads?q=" + PHONE_PREFIX))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.totalElements", v -> v.assertThat().asNumber().isEqualTo(2))
				.hasPathSatisfying("$.content[0].maskedName", v -> v.assertThat().isEqualTo("김*수"))
				.hasPathSatisfying("$.content[0].maskedPhone", v -> v.assertThat().isEqualTo("010-****-2222"))
				.hasPathSatisfying("$.content[1].maskedName", v -> v.assertThat().isEqualTo("홍*동"))
				.hasPathSatisfying("$.content[0].vehicleName", v -> v.assertThat().isEqualTo("기아 쏘렌토"))
				.hasPathSatisfying("$.content[0].createdAt", v -> v.assertThat().asString().endsWith("+09:00"));

		String body = contentOf(get("/api/admin/leads?q=" + PHONE_PREFIX));
		assertThat(body).doesNotContain("홍길동", "01080001111");
	}

	@Test
	void 목록은_상태_유형_기간_이름_연락처로_거를_수_있다() {
		insertLead("홍길동", "01080001111", "QUICK", "NEW", "2030-01-10 10:00:00+09");
		insertLead("김철수", "01080002222", "ESTIMATE", "IN_PROGRESS", "2030-01-11 23:59:00+09");
		insertLead("박영희", "01080003333", "DEAL", "CONTRACTED", "2030-01-12 00:00:00+09");

		assertThat(totalOf("/api/admin/leads?q=0108&status=IN_PROGRESS")).isEqualTo(1);
		assertThat(totalOf("/api/admin/leads?q=0108&type=DEAL")).isEqualTo(1);
		// 한국 시간 기준 하루 단위: 11일 23:59 는 포함, 12일 00:00 은 제외
		assertThat(totalOf("/api/admin/leads?q=0108&from=2030-01-11&to=2030-01-11")).isEqualTo(1);
		assertThat(totalOf("/api/admin/leads?q=0108-0003")).as("하이픈 섞인 연락처 검색").isEqualTo(1);
		assertThat(totalOf("/api/admin/leads?q=영희")).isEqualTo(1);
		assertThat(totalOf("/api/admin/leads?q=0108&size=2")).isEqualTo(3);
	}

	@Test
	void 검색어의_퍼센트와_밑줄은_와일드카드가_아니라_글자로_찾는다() {
		insertLead("홍길동", "01080001111", "QUICK", "NEW", "2030-01-10 10:00:00+09");
		insertLead("50%_할인고객", "01080002222", "QUICK", "NEW", "2030-01-10 11:00:00+09");

		// 검색어를 주소 문자열에 직접 넣으면 한 번 더 인코딩되므로 param() 으로 원래 글자 그대로 보낸다
		assertThat(totalWithKeyword("%")).as("% 한 글자로 전체가 검색되면 안 됨").isEqualTo(1);
		assertThat(totalWithKeyword("_")).as("_ 한 글자로 전체가 검색되면 안 됨").isEqualTo(1);
		assertThat(totalWithKeyword("50%_")).isEqualTo(1);
		assertThat(totalWithKeyword("길동")).isEqualTo(1);
	}

	/** 테스트 데이터(0108…) 가 아닌 샘플 리드와 섞이지 않도록 기간을 2030-01-10 로 한정 */
	private long totalWithKeyword(String keyword) {
		MvcTestResult result = mvc.get().uri("/api/admin/leads")
				.param("q", keyword)
				.param("from", "2030-01-10")
				.param("to", "2030-01-10")
				.with(user(admin))
				.exchange();
		Number total = com.jayway.jsonpath.JsonPath.read(contentOf(result), "$.totalElements");
		return total.longValue();
	}

	// ---------------------------------------------------------------- 상세·상태·메모

	@Test
	void 상세는_전체_정보를_주고_조회기록을_남긴다() {
		long leadId = insertLead("홍길동", "01080001111", "ESTIMATE", "NEW", "2030-01-10 10:00:00+09");

		assertThat(get("/api/admin/leads/" + leadId))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.name", v -> v.assertThat().isEqualTo("홍길동"))
				.hasPathSatisfying("$.phone", v -> v.assertThat().isEqualTo("010-8000-1111"))
				.hasPathSatisfying("$.conditionSummary", v -> v.assertThat().isEqualTo("장기렌트 · 48개월"))
				.hasPathSatisfying("$.vehicleSnapshot.trim", v -> v.assertThat().isEqualTo("프레스티지"));

		assertThat(jdbc.queryForObject(
				"select count(*) from admin_access_log where action = 'LEAD_VIEW' and target_id = ? and admin_id = ?",
				Long.class, leadId, admin.getId())).isEqualTo(1);
	}

	@Test
	void 상태를_바꾸고_메모를_남기면_상세에_작성자와_함께_보인다() {
		long leadId = insertLead("홍길동", "01080001111", "QUICK", "NEW", "2030-01-10 10:00:00+09");

		assertThat(mvc.patch().uri("/api/admin/leads/{id}", leadId).with(user(admin)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"NO_ANSWER\"}"))
				.hasStatus(204);
		assertThat(mvc.post().uri("/api/admin/leads/{id}/notes", leadId).with(user(admin)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"  1차 통화 부재, 오후 재연락  \"}"))
				.hasStatus(201)
				.bodyJson().extractingPath("$.adminName").isEqualTo("김관리");

		assertThat(get("/api/admin/leads/" + leadId))
				.bodyJson()
				.hasPathSatisfying("$.status", v -> v.assertThat().isEqualTo("NO_ANSWER"))
				.hasPathSatisfying("$.notes[0].content", v -> v.assertThat().isEqualTo("1차 통화 부재, 오후 재연락"))
				.hasPathSatisfying("$.notes[0].adminName", v -> v.assertThat().isEqualTo("김관리"));
	}

	@Test
	void 잘못된_상태값과_빈_메모_없는_리드는_거부한다() {
		long leadId = insertLead("홍길동", "01080001111", "QUICK", "NEW", "2030-01-10 10:00:00+09");

		assertThat(mvc.patch().uri("/api/admin/leads/{id}", leadId).with(user(admin)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DONE\"}"))
				.hasStatus(400);
		assertThat(mvc.post().uri("/api/admin/leads/{id}/notes", leadId).with(user(admin)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"   \"}"))
				.hasStatus(400);
		assertThat(get("/api/admin/leads/99999999")).hasStatus(404);
	}

	// ---------------------------------------------------------------- CSV · 대시보드

	@Test
	void CSV는_엑셀용_BOM과_전체정보를_담고_수식은_무력화하며_다운로드기록을_남긴다() throws Exception {
		insertLead("=HYPERLINK(\"x\")", "01080001111", "QUICK", "NEW", "2030-01-10 10:00:00+09");

		MvcTestResult result = get("/api/admin/leads/export.csv?q=" + PHONE_PREFIX);
		assertThat(result).hasStatusOk();
		assertThat(result.getResponse().getContentType()).startsWith("text/csv");
		assertThat(result.getResponse().getHeader("Content-Disposition")).contains("attachment", ".csv");

		byte[] bytes = result.getResponse().getContentAsByteArray();
		assertThat(bytes).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
		String csv = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
		List<String> lines = csv.lines().toList();
		assertThat(lines.get(0)).startsWith("번호,신청일시,유형,상태,이름,연락처");
		assertThat(lines.get(1)).contains("010-8000-1111", "\"'=HYPERLINK(\"\"x\"\")\"", "장기렌트 · 48개월");

		assertThat(jdbc.queryForObject("select count(*) from admin_access_log where action = 'LEAD_EXPORT' and admin_id = ?",
				Long.class, admin.getId())).isEqualTo(1);
	}

	@Test
	void 대시보드는_기간별_건수와_모든_상태의_건수와_최근신청을_준다() {
		insertLead("홍길동", "01080001111", "QUICK", "NEW", null);

		assertThat(get("/api/admin/dashboard"))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.todayCount", v -> v.assertThat().asNumber().isNotEqualTo(0))
				.hasPathSatisfying("$.statusCounts", v -> v.assertThat().asMap()
						.containsKeys("NEW", "IN_PROGRESS", "CONTRACTED", "NO_ANSWER", "CANCELED"))
				.hasPathSatisfying("$.recentLeads[0].maskedName", v -> v.assertThat().isEqualTo("홍*동"));
	}

	// ---------------------------------------------------------------- helpers

	private MvcTestResult get(String uri) {
		return mvc.get().uri(uri).with(user(admin)).exchange();
	}

	private long totalOf(String uri) {
		try {
			Number total = com.jayway.jsonpath.JsonPath.read(contentOf(get(uri)), "$.totalElements");
			return total.longValue();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private String contentOf(MvcTestResult result) {
		try {
			return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		}
		catch (java.io.UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	/** createdAt 이 null 이면 지금 시각 */
	private long insertLead(String name, String phone, String type, String status, String createdAt) {
		return jdbc.queryForObject("""
				insert into lead (type, name, phone, vehicle_snapshot, conditions, agree_privacy, agreed_at, status,
				                  created_at, updated_at)
				values (?, ?, ?, '{"brand":"기아","model":"쏘렌토","trim":"프레스티지"}'::jsonb,
				        '{"useType":"RENT","periodMonths":48}'::jsonb, true, now(), ?,
				        coalesce(?::timestamptz, now()), now())
				returning id""", Long.class, type, name, phone, status, createdAt);
	}

}
