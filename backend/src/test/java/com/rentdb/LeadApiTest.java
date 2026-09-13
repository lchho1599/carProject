package com.rentdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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

import com.jayway.jsonpath.JsonPath;

/**
 * 3단계 상담 신청 API 통합 테스트.
 * - 로컬 DB(5433)와 Mailpit(1025/8025)이 실행 중이어야 한다.
 * - 커밋 후 알림(비동기)을 검증해야 하므로 트랜잭션 롤백을 쓰지 않고, 테스트용 연락처(0109로 시작)를 끝나고 지운다.
 */
@SpringBootTest(properties = "app.lead.rate-limit.max-requests=1000")
@AutoConfigureMockMvc
class LeadApiTest {

	private static final String TEST_PHONE_PREFIX = "0109";
	private static final String MAILPIT = "http://localhost:8025/api/v1";

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JdbcTemplate jdbc;

	private final HttpClient http = HttpClient.newHttpClient();

	@BeforeEach
	void setUp() {
		setNotifyEmails("[]");
	}

	@AfterEach
	void cleanUp() {
		jdbc.update("delete from notification_log where lead_id in (select id from lead where phone like ?)",
				TEST_PHONE_PREFIX + "%");
		jdbc.update("delete from lead where phone like ?", TEST_PHONE_PREFIX + "%");
		jdbc.update("delete from deal where title like '테스트_%'");
		setNotifyEmails("[]");
	}

	// ---------------------------------------------------------------- 유형별 신청

	@Test
	void 빠른상담은_모델만_골라도_브랜드와_함께_저장하고_utm은_utm_키만_남긴다() {
		long modelId = jdbc.queryForObject("select id from vehicle_model where name = '그랜저'", Long.class);
		String phone = newPhone();

		long leadId = createdId(post("""
				{"type":"QUICK","name":"이빠른","phone":"%s","agreePrivacy":true,
				 "modelId":%d,"conditions":{"periodMonths":48},
				 "sourceUrl":"/","utm":{"utm_source":"naver","utm_campaign":"grandeur","foo":"bar"}}
				""".formatted(phone, modelId)));

		Map<String, Object> row = jdbc.queryForMap("""
				select l.type, l.brand_id, l.model_id, l.trim_id, l.vehicle_snapshot::text as snapshot,
				       l.conditions::text as conditions, l.utm::text as utm, l.ip_hash, l.status
				from lead l where l.id = ?""", leadId);
		assertThat(row.get("type")).isEqualTo("QUICK");
		assertThat(row.get("brand_id")).isNotNull();
		assertThat(row.get("trim_id")).isNull();
		assertThat((String) row.get("snapshot")).contains("\"brand\": \"현대\"", "\"model\": \"그랜저\"");
		assertThat((String) row.get("conditions")).contains("\"periodMonths\": 48");
		assertThat((String) row.get("utm")).contains("utm_source", "utm_campaign").doesNotContain("foo");
		assertThat((String) row.get("ip_hash")).hasSize(64);
		assertThat(row.get("status")).isEqualTo("NEW");
	}

	@Test
	void 간편견적은_서버가_트림가_색상추가금_옵션가로_합계를_계산한다() {
		long trimId = jdbc.queryForObject("""
				select t.id from vehicle_trim t join vehicle_model m on m.id = t.model_id
				where m.name = '쏘렌토' order by t.sort_order limit 1""", Long.class);
		long trimPrice = jdbc.queryForObject("select price from vehicle_trim where id = ?", Long.class, trimId);
		Map<String, Object> color = jdbc.queryForMap("""
				select c.id, c.extra_price from model_color c join vehicle_model m on m.id = c.model_id
				where m.name = '쏘렌토' and c.extra_price > 0 limit 1""");
		List<Map<String, Object>> options = jdbc.queryForList("""
				select o.id, o.price from model_option o join vehicle_model m on m.id = o.model_id
				where m.name = '쏘렌토' order by o.sort_order limit 2""");
		long expectedTotal = trimPrice + ((Number) color.get("extra_price")).longValue()
				+ options.stream().mapToLong(o -> ((Number) o.get("price")).longValue()).sum();

		long leadId = createdId(post("""
				{"type":"ESTIMATE","name":"김견적","phone":"%s","agreePrivacy":true,"agreeMarketing":true,
				 "trimId":%d,"colorId":%d,"optionIds":[%d,%d],
				 "conditions":{"useType":"RENT","periodMonths":48,"depositRate":0,"prepayRate":30,
				               "insuranceAge":26,"annualMileage":20000,"creditScore":"OVER_700"}}
				""".formatted(newPhone(), trimId, color.get("id"), options.get(0).get("id"), options.get(1).get("id"))));

		Map<String, Object> row = jdbc.queryForMap(
				"select total_price, agree_marketing, vehicle_snapshot::text as snapshot from lead where id = ?", leadId);
		assertThat(((Number) row.get("total_price")).longValue()).isEqualTo(expectedTotal);
		assertThat(row.get("agree_marketing")).isEqualTo(true);
		assertThat((String) row.get("snapshot")).contains("\"trim\"", "\"color\"", "\"options\"", "\"trimPrice\"");
	}

	@Test
	void 간편견적에서_다른_모델의_옵션이나_트림_누락은_거부한다() {
		long sorentoTrimId = jdbc.queryForObject("""
				select t.id from vehicle_trim t join vehicle_model m on m.id = t.model_id
				where m.name = '쏘렌토' limit 1""", Long.class);
		long k5OptionId = jdbc.queryForObject("""
				select o.id from model_option o join vehicle_model m on m.id = o.model_id
				where m.name = 'K5' limit 1""", Long.class);

		assertThat(post("""
				{"type":"ESTIMATE","name":"김옵션","phone":"%s","agreePrivacy":true,"trimId":%d,"optionIds":[%d]}
				""".formatted(newPhone(), sorentoTrimId, k5OptionId)))
				.hasStatus(400)
				.bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_FAILED");

		assertThat(post("""
				{"type":"ESTIMATE","name":"김트림","phone":"%s","agreePrivacy":true}
				""".formatted(newPhone())))
				.hasStatus(400);
	}

	@Test
	void 특가_신청은_노출중인_특가만_가능하고_특가_정보를_스냅샷에_남긴다() {
		long visibleDealId = jdbc.queryForObject(
				"select id from deal where type = 'NO_DEPOSIT' and is_published and ends_at is null limit 1", Long.class);
		long expiredDealId = jdbc.queryForObject("""
				insert into deal (type, trim_id, title, monthly_price, period_months, starts_at, ends_at, is_published)
				select 'TIME_SALE', id, '테스트_종료특가', 100000, 48, now() - interval '3 days', now() - interval '1 day', true
				from vehicle_trim limit 1 returning id""", Long.class);

		long leadId = createdId(post("""
				{"type":"DEAL","name":"박특가","phone":"%s","agreePrivacy":true,"dealId":%d}
				""".formatted(newPhone(), visibleDealId)));
		assertThat(jdbc.queryForObject("select vehicle_snapshot::text from lead where id = ?", String.class, leadId))
				.contains("\"deal\"", "\"monthlyPrice\"", "\"trim\"");

		assertThat(post("""
				{"type":"DEAL","name":"박종료","phone":"%s","agreePrivacy":true,"dealId":%d}
				""".formatted(newPhone(), expiredDealId)))
				.hasStatus(400);
	}

	@Test
	void 판매완료된_즉시출고_차량은_신청할_수_없다() {
		long soldId = jdbc.queryForObject("select id from instant_stock where status = 'SOLD' limit 1", Long.class);
		long availableId = jdbc.queryForObject(
				"select id from instant_stock where status = 'AVAILABLE' and is_published limit 1", Long.class);

		assertThat(post("""
				{"type":"INSTANT","name":"최판매","phone":"%s","agreePrivacy":true,"instantStockId":%d}
				""".formatted(newPhone(), soldId)))
				.hasStatus(400);

		long leadId = createdId(post("""
				{"type":"INSTANT","name":"최출고","phone":"%s","agreePrivacy":true,"instantStockId":%d}
				""".formatted(newPhone(), availableId)));
		assertThat(jdbc.queryForObject("select instant_stock_id from lead where id = ?", Long.class, leadId))
				.isEqualTo(availableId);
	}

	// ---------------------------------------------------------------- 검증·중복·스팸

	@Test
	void 필수동의_누락과_잘못된_연락처는_필드별_오류를_준다() {
		assertThat(post("""
				{"type":"QUICK","name":"정검증","phone":"02-123-4567","agreePrivacy":false}
				"""))
				.hasStatus(400)
				.bodyJson()
				.hasPathSatisfying("$.code", v -> v.assertThat().isEqualTo("VALIDATION_FAILED"))
				.hasPathSatisfying("$.fieldErrors[*].field", v -> v.assertThat().asArray()
						.contains("agreePrivacy", "phone"));
	}

	@Test
	void 같은_연락처로_10분_안에_다시_신청하면_409() {
		String phone = newPhone();
		String hyphenated = phone.substring(0, 3) + "-" + phone.substring(3, 7) + "-" + phone.substring(7);

		assertThat(post("""
				{"type":"QUICK","name":"한중복","phone":"%s","agreePrivacy":true}""".formatted(phone)))
				.hasStatus(201);

		assertThat(post("""
				{"type":"QUICK","name":"한중복","phone":"%s","agreePrivacy":true}""".formatted(hyphenated)))
				.hasStatus(409)
				.bodyJson().extractingPath("$.code").isEqualTo("LEAD_DUPLICATED");
	}

	@Test
	void 허니팟_필드가_채워지면_저장하지_않고_성공처럼_응답한다() {
		String phone = newPhone();

		assertThat(post("""
				{"type":"QUICK","name":"봇봇","phone":"%s","agreePrivacy":true,"website":"http://spam"}
				""".formatted(phone)))
				.hasStatus(201);

		assertThat(jdbc.queryForObject("select count(*) from lead where phone = ?", Long.class, phone)).isZero();
	}

	// ---------------------------------------------------------------- 알림 메일

	@Test
	void 신청하면_운영자에게_마스킹된_알림메일이_발송되고_기록이_남는다() throws Exception {
		setNotifyEmails("[\"ops1@rentdb.test\", \"ops2@rentdb.test\"]");
		clearMailpit();
		long trimId = jdbc.queryForObject("""
				select t.id from vehicle_trim t join vehicle_model m on m.id = t.model_id
				where m.name = 'EV3' limit 1""", Long.class);
		String phone = newPhone();

		long leadId = createdId(post("""
				{"type":"ESTIMATE","name":"홍길동","phone":"%s","agreePrivacy":true,"trimId":%d,
				 "conditions":{"useType":"LEASE","periodMonths":36,"annualMileage":0}}
				""".formatted(phone, trimId)));

		List<Map<String, Object>> logs = waitForNotificationLogs(leadId, 2);
		assertThat(logs).extracting(log -> log.get("status")).containsOnly("SENT");
		assertThat(logs).extracting(log -> log.get("recipient"))
				.containsExactlyInAnyOrder("ops1@rentdb.test", "ops2@rentdb.test");

		String messages = httpGet(MAILPIT + "/messages");
		List<String> subjects = JsonPath.read(messages, "$.messages[*].Subject");
		assertThat(subjects).hasSize(2).allSatisfy(subject ->
				assertThat(subject).isEqualTo("[신규 상담] 간편견적 · 기아 EV3 · 홍*동"));

		String messageId = JsonPath.read(messages, "$.messages[0].ID");
		String html = JsonPath.read(httpGet(MAILPIT + "/message/" + messageId), "$.HTML");
		String maskedPhone = "010-****-" + phone.substring(7);
		assertThat(html)
				.contains(maskedPhone, "홍*동", "/admin/leads/" + leadId, "리스", "36개월", "주행거리 무제한")
				.doesNotContain(phone, "홍길동");
	}

	// ---------------------------------------------------------------- helpers

	private MvcTestResult post(String json) {
		return mvc.post().uri("/api/leads")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.exchange();
	}

	private long createdId(MvcTestResult result) {
		assertThat(result).hasStatus(201);
		try {
			Number id = JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
			return id.longValue();
		}
		catch (java.io.UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	private String newPhone() {
		return TEST_PHONE_PREFIX + "%07d".formatted(ThreadLocalRandom.current().nextInt(10_000_000));
	}

	private void setNotifyEmails(String json) {
		jdbc.update("update app_setting set value = ?::jsonb where setting_key = 'notify_emails'", json);
	}

	private List<Map<String, Object>> waitForNotificationLogs(long leadId, int expected) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 15_000;
		List<Map<String, Object>> logs = List.of();
		while (System.currentTimeMillis() < deadline) {
			logs = jdbc.queryForList("select recipient, status, error_message from notification_log where lead_id = ?",
					leadId);
			if (logs.size() >= expected) {
				return logs;
			}
			Thread.sleep(200);
		}
		throw new AssertionError("알림 발송 기록이 " + expected + "건 생기지 않았습니다. 현재: " + logs);
	}

	private void clearMailpit() throws Exception {
		http.send(HttpRequest.newBuilder(URI.create(MAILPIT + "/messages")).DELETE().build(),
				HttpResponse.BodyHandlers.discarding());
	}

	private String httpGet(String url) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
	}

}
