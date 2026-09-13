package com.rentdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import com.jayway.jsonpath.JsonPath;
import com.rentdb.admin.domain.AdminUser;
import com.rentdb.admin.repository.AdminUserRepository;
import com.rentdb.global.security.AdminPrincipal;

/**
 * 8단계 관리자 상품 관리 API 테스트 — 테스트마다 트랜잭션을 롤백한다 (업로드 파일은 임시 폴더에 저장 후 삭제).
 */
@SpringBootTest(properties = "app.storage.local-dir=build/test-uploads")
@AutoConfigureMockMvc
@Transactional
class AdminCatalogApiTest {

	private static final Path UPLOAD_DIR = Path.of("build/test-uploads");
	private static final byte[] PNG_HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };

	@Autowired
	MockMvcTester mvc;

	@Autowired
	AdminUserRepository adminUserRepository;

	private AdminPrincipal admin;

	@BeforeEach
	void setUp() {
		AdminUser saved = adminUserRepository.save(AdminUser.builder()
				.email("catalog-" + UUID.randomUUID().toString().substring(0, 8) + "@catalog.test")
				.passwordHash("{noop}unused")
				.name("상품관리자")
				.build());
		admin = AdminPrincipal.from(saved);
	}

	@AfterAll
	static void removeUploads() throws Exception {
		FileSystemUtils.deleteRecursively(UPLOAD_DIR);
	}

	// ---------------------------------------------------------------- 차량

	@Test
	void 브랜드_모델_트림_옵션_색상을_등록하고_사용자_화면에_반영된다() {
		long brandId = idOf(post("/api/admin/brands",
				"{\"name\":\"테스트모터스\",\"origin\":\"DOMESTIC\",\"sortOrder\":99,\"active\":true}"), 201);
		long modelId = idOf(post("/api/admin/models", """
				{"brandId":%d,"name":"테스트카","segment":"중형","bodyType":"SUV","fuel":"EV","sortOrder":1,"active":true}
				""".formatted(brandId)), 201);
		long trimId = idOf(post("/api/admin/models/" + modelId + "/trims",
				"{\"name\":\"기본형\",\"price\":35000000,\"sortOrder\":1,\"active\":true}"), 201);
		post("/api/admin/models/" + modelId + "/options", "{\"name\":\"선루프\",\"price\":1000000,\"sortOrder\":1,\"active\":true}");
		assertThat(post("/api/admin/models/" + modelId + "/colors",
				"{\"name\":\"화이트\",\"hexCode\":\"#ffffff\",\"extraPrice\":50000,\"sortOrder\":1,\"active\":true}"))
				.hasStatus(201)
				.bodyJson().extractingPath("$.hexCode").isEqualTo("#FFFFFF");

		assertThat(mvc.get().uri("/api/models/" + modelId))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.brand.name", v -> v.assertThat().isEqualTo("테스트모터스"))
				.hasPathSatisfying("$.trims[0].id", v -> v.assertThat().asNumber().isEqualTo((int) trimId))
				.hasPathSatisfying("$.options", v -> v.assertThat().asArray().hasSize(1))
				.hasPathSatisfying("$.colors", v -> v.assertThat().asArray().hasSize(1));
	}

	@Test
	void 사용_안함으로_바꾸면_사용자_화면에서_숨고_관리자_목록에는_남는다() {
		long brandId = idOf(post("/api/admin/brands",
				"{\"name\":\"숨김모터스\",\"origin\":\"IMPORTED\",\"sortOrder\":0,\"active\":true}"), 201);

		assertThat(put("/api/admin/brands/" + brandId,
				"{\"name\":\"숨김모터스\",\"origin\":\"IMPORTED\",\"sortOrder\":0,\"active\":false}"))
				.hasStatusOk()
				.bodyJson().extractingPath("$.active").isEqualTo(false);

		assertThat(mvc.get().uri("/api/brands?origin=IMPORTED"))
				.bodyJson().extractingPath("$[*].name").asArray().doesNotContain("숨김모터스");
		assertThat(mvc.get().uri("/api/admin/brands").with(user(admin)))
				.bodyJson().extractingPath("$[*].name").asArray().contains("숨김모터스");
	}

	@Test
	void 브랜드명과_같은_브랜드의_모델명은_중복_등록할_수_없다() {
		assertThat(post("/api/admin/brands", "{\"name\":\"기아\",\"origin\":\"DOMESTIC\",\"sortOrder\":0,\"active\":true}"))
				.hasStatus(409)
				.bodyJson().extractingPath("$.code").isEqualTo("DUPLICATED");
	}

	@Test
	void 필수값_누락과_위험한_이미지_주소는_400() {
		assertThat(post("/api/admin/brands", "{\"name\":\"\",\"sortOrder\":0,\"active\":true}"))
				.hasStatus(400)
				.bodyJson().extractingPath("$.fieldErrors[*].field").asArray().contains("name", "origin");

		assertThat(post("/api/admin/brands",
				"{\"name\":\"위험모터스\",\"origin\":\"DOMESTIC\",\"logoUrl\":\"javascript:alert(1)\",\"sortOrder\":0,\"active\":true}"))
				.hasStatus(400)
				.bodyJson().extractingPath("$.fieldErrors[0].field").isEqualTo("logoUrl");
	}

	// ---------------------------------------------------------------- 특가

	@Test
	void 특가를_등록하면_노출상태를_계산하고_수정_삭제할_수_있다() {
		long trimId = firstTrimId();
		long dealId = idOf(post("/api/admin/deals", dealJson(trimId, "2020-01-01T00:00:00+09:00", "2020-02-01T00:00:00+09:00", true)), 201);

		assertThat(mvc.get().uri("/api/admin/deals/" + dealId).with(user(admin)))
				.bodyJson()
				.hasPathSatisfying("$.displayStatus", v -> v.assertThat().isEqualTo("ENDED"))
				.hasPathSatisfying("$.startsAt", v -> v.assertThat().asString().endsWith("+09:00"));

		assertThat(put("/api/admin/deals/" + dealId, dealJson(trimId, "2020-01-01T00:00:00+09:00", null, true)))
				.hasStatusOk()
				.bodyJson().extractingPath("$.displayStatus").isEqualTo("VISIBLE");
		assertThat(mvc.get().uri("/api/deals?type=TIME_SALE"))
				.bodyJson().extractingPath("$[*].title").asArray().contains("테스트 특가");

		assertThat(mvc.delete().uri("/api/admin/deals/" + dealId).with(user(admin)).with(csrf())).hasStatus(204);
		assertThat(mvc.get().uri("/api/admin/deals/" + dealId).with(user(admin))).hasStatus(404);
	}

	@Test
	void 특가_종료일시가_시작보다_앞서거나_기간이_허용값이_아니면_400() {
		long trimId = firstTrimId();
		assertThat(post("/api/admin/deals", dealJson(trimId, "2030-02-01T00:00:00+09:00", "2030-01-01T00:00:00+09:00", true)))
				.hasStatus(400)
				.bodyJson().extractingPath("$.message").asString().contains("종료 일시");
		assertThat(post("/api/admin/deals",
				dealJson(trimId, "2030-01-01T00:00:00+09:00", null, true).replace("\"periodMonths\":48", "\"periodMonths\":50")))
				.hasStatus(400);
	}

	// ---------------------------------------------------------------- 즉시출고·배너

	@Test
	void 즉시출고를_판매완료로_바꾸면_사용자_목록에서_빠진다() {
		long trimId = firstTrimId();
		String body = """
				{"trimId":%d,"exteriorColor":"테스트블루","vehiclePrice":30000000,"monthlyPrice":300000,
				 "conditionText":"48개월 / 선납금 30%% 기준","status":"%s","sortOrder":0,"published":true}
				""";
		long stockId = idOf(post("/api/admin/instant", body.formatted(trimId, "AVAILABLE")), 201);
		assertThat(mvc.get().uri("/api/instant"))
				.bodyJson().extractingPath("$.items[*].exteriorColor").asArray().contains("테스트블루");

		assertThat(put("/api/admin/instant/" + stockId, body.formatted(trimId, "SOLD")))
				.hasStatusOk()
				.bodyJson().extractingPath("$.visible").isEqualTo(false);
		assertThat(mvc.get().uri("/api/instant"))
				.bodyJson().extractingPath("$.items[*].exteriorColor").asArray().doesNotContain("테스트블루");
	}

	@Test
	void 배너는_사이트경로나_https_주소만_링크로_허용한다() {
		String banner = """
				{"title":"테스트 배너","imagePcUrl":"/images/sample/banner-1-pc.jpg","imageMobileUrl":"/images/sample/banner-1-mobile.jpg",
				 "linkUrl":"%s","startsAt":"2020-01-01T00:00:00+09:00","sortOrder":0,"published":true}
				""";
		assertThat(post("/api/admin/banners", banner.formatted("/deals?type=time")))
				.hasStatus(201)
				.bodyJson().extractingPath("$.displayStatus").isEqualTo("VISIBLE");

		for (String dangerous : new String[] { "javascript:alert(1)", "//evil.example", "http://insecure.example" }) {
			assertThat(post("/api/admin/banners", banner.formatted(dangerous)))
					.as(dangerous).hasStatus(400)
					.bodyJson().extractingPath("$.fieldErrors[0].field").isEqualTo("linkUrl");
		}
	}

	// ---------------------------------------------------------------- 업로드

	@Test
	void 이미지를_업로드하면_공개주소로_제공된다() throws Exception {
		MockMultipartFile png = new MockMultipartFile("file", "car.png", "image/png", PNG_HEADER);

		MvcTestResult result = mvc.post().uri("/api/admin/uploads").multipart().file(png)
				.with(user(admin)).with(csrf()).exchange();
		assertThat(result).hasStatus(201);
		String url = JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.url");
		assertThat(url).matches("^/api/files/\\d{4}/\\d{2}/[0-9a-f-]{36}\\.png$");

		MvcTestResult file = mvc.get().uri(url).exchange();
		assertThat(file).hasStatusOk();
		assertThat(file.getResponse().getContentAsByteArray()).isEqualTo(PNG_HEADER);
		assertThat(file.getResponse().getHeader("Cache-Control")).contains("max-age");
		assertThat(Files.exists(UPLOAD_DIR.resolve(url.substring("/api/files/".length())))).isTrue();
	}

	@Test
	void 이미지로_위장한_파일과_5MB_초과는_거부하고_로그인하지_않으면_업로드할_수_없다() {
		MockMultipartFile fake = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
				"<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));
		assertThat(mvc.post().uri("/api/admin/uploads").multipart().file(fake).with(user(admin)).with(csrf()))
				.hasStatus(400)
				.bodyJson().extractingPath("$.message").asString().contains("JPG, PNG, WEBP");

		byte[] big = new byte[5 * 1024 * 1024 + 10];
		System.arraycopy(PNG_HEADER, 0, big, 0, PNG_HEADER.length);
		assertThat(mvc.post().uri("/api/admin/uploads").multipart()
				.file(new MockMultipartFile("file", "big.png", "image/png", big)).with(user(admin)).with(csrf()))
				.hasStatus(400);

		// 보안 필터 순서상 CSRF 토큰 검사가 먼저: 토큰 없으면 403, 토큰은 있어도 로그인 안 했으면 401
		assertThat(mvc.post().uri("/api/admin/uploads").multipart()
				.file(new MockMultipartFile("file", "car.png", "image/png", PNG_HEADER)))
				.hasStatus(403);
		assertThat(mvc.post().uri("/api/admin/uploads").multipart()
				.file(new MockMultipartFile("file", "car.png", "image/png", PNG_HEADER)).with(csrf()))
				.hasStatus(401);
	}

	@Test
	void 파일_주소를_조작해도_저장_폴더_밖의_파일은_읽을_수_없다() {
		// 인코딩된 경로 구분자는 Spring Security 방화벽이 컨트롤러 도달 전에 400 으로 차단한다
		MvcTestResult traversal = mvc.get().uri("/api/files/2026/09/..%2F..%2Fapplication.yml").exchange();
		assertThat(traversal.getResponse().getStatus()).isIn(400, 404);
		// 서버가 만든 형식(UUID 파일명)이 아니면 파일을 찾지 않는다
		assertThat(mvc.get().uri("/api/files/2026/09/not-a-uuid.png")).hasStatus(404);
	}

	// ---------------------------------------------------------------- helpers

	private MvcTestResult post(String uri, String json) {
		return mvc.post().uri(uri).with(user(admin)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private MvcTestResult put(String uri, String json) {
		return mvc.put().uri(uri).with(user(admin)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private long idOf(MvcTestResult result, int expectedStatus) {
		assertThat(result).hasStatus(expectedStatus);
		try {
			Number id = JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
			return id.longValue();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private long firstTrimId() {
		try {
			String body = mvc.get().uri("/api/admin/models/9").with(user(admin)).exchange()
					.getResponse().getContentAsString(StandardCharsets.UTF_8);
			Number id = JsonPath.read(body, "$.trims[0].id");
			return id.longValue();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static String dealJson(long trimId, String startsAt, String endsAt, boolean published) {
		return """
				{"type":"TIME_SALE","trimId":%d,"title":"테스트 특가","badge":"HOT","originalMonthly":300000,"monthlyPrice":250000,
				 "periodMonths":48,"depositRate":0,"prepayRate":30,"startsAt":"%s","endsAt":%s,"sortOrder":0,"published":%s}
				""".formatted(trimId, startsAt, endsAt == null ? "null" : "\"" + endsAt + "\"", published);
	}

}
