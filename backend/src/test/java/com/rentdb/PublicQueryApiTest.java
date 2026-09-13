package com.rentdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.rentdb.deal.domain.Deal;
import com.rentdb.deal.domain.DealType;
import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.VehicleModel;
import com.rentdb.vehicle.domain.VehicleTrim;

import jakarta.persistence.EntityManager;

/**
 * 2단계 공개 조회 API 테스트 (로컬 DB 샘플 데이터 기준).
 * 테스트마다 트랜잭션을 롤백하므로 테스트 중 추가·변경한 데이터는 남지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PublicQueryApiTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	EntityManager em;

	// ---------------------------------------------------------------- 브랜드·모델

	@Test
	void 브랜드_목록은_정렬순서대로_국산_수입을_거를_수_있다() {
		assertThat(mvc.get().uri("/api/brands"))
				.hasStatusOk()
				.bodyJson().extractingPath("$[*].name").asArray()
				.containsExactly("현대", "기아", "제네시스", "르노코리아", "BMW", "벤츠");

		assertThat(mvc.get().uri("/api/brands?origin=IMPORTED"))
				.hasStatusOk()
				.bodyJson().extractingPath("$[*].name").asArray()
				.containsExactly("BMW", "벤츠");
	}

	@Test
	void 브랜드의_모델_목록에_최저가가_포함된다() {
		assertThat(mvc.get().uri("/api/brands/{id}/models", brandId("기아")))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$[*].name", v -> v.assertThat().asArray()
						.containsExactly("K5", "스포티지", "쏘렌토", "카니발", "EV3"))
				.hasPathSatisfying("$[0].minPrice", v -> v.assertThat().asNumber().isEqualTo(27200000));
	}

	@Test
	void 비활성_모델은_목록에서_빠진다() {
		em.createQuery("update VehicleModel m set m.active = false where m.id = :id")
				.setParameter("id", model("K5").getId()).executeUpdate();

		assertThat(mvc.get().uri("/api/brands/{id}/models", brandId("기아")))
				.hasStatusOk()
				.bodyJson().extractingPath("$[*].name").asArray()
				.doesNotContain("K5")
				.hasSize(4);
	}

	@Test
	void 모델_상세는_트림_옵션_색상을_함께_준다() {
		assertThat(mvc.get().uri("/api/models/{id}", model("쏘렌토").getId()))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.brand.name", v -> v.assertThat().isEqualTo("기아"))
				.hasPathSatisfying("$.bodyType", v -> v.assertThat().isEqualTo("SUV"))
				.hasPathSatisfying("$.trims", v -> v.assertThat().asArray().hasSize(2))
				.hasPathSatisfying("$.options", v -> v.assertThat().asArray().hasSize(4))
				.hasPathSatisfying("$.colors[0].hexCode", v -> v.assertThat().isEqualTo("#F4F4F2"));
	}

	@Test
	void 없는_모델은_404와_오류코드를_준다() {
		assertThat(mvc.get().uri("/api/models/999999"))
				.hasStatus(404)
				.bodyJson().extractingPath("$.code").isEqualTo("NOT_FOUND");
	}

	@Test
	void 허용되지_않는_파라미터는_400을_준다() {
		assertThat(mvc.get().uri("/api/brands?origin=CHINA"))
				.hasStatus(400)
				.bodyJson()
				.hasPathSatisfying("$.code", v -> v.assertThat().isEqualTo("VALIDATION_FAILED"))
				.hasPathSatisfying("$.fieldErrors[0].field", v -> v.assertThat().isEqualTo("origin"));

		assertThat(mvc.get().uri("/api/models/abc")).hasStatus(400);
	}

	// ---------------------------------------------------------------- 특가

	@Test
	void 특가는_유형으로_거르고_차량_요약을_포함한다() {
		assertThat(mvc.get().uri("/api/deals?type=NO_DEPOSIT"))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$[*].type", v -> v.assertThat().asArray().isNotEmpty().containsOnly("NO_DEPOSIT"))
				.hasPathSatisfying("$[0].vehicle.brandName", v -> v.assertThat().isNotNull())
				.hasPathSatisfying("$[0].vehicle.trimPrice", v -> v.assertThat().asNumber().isNotNull());
	}

	@Test
	void 특가는_공개되고_노출기간_안에_있는_것만_보인다() throws Exception {
		OffsetDateTime now = OffsetDateTime.now();
		VehicleTrim trim = trimOf("아반떼");
		saveDeal(trim, "노출중_테스트", now.minusDays(1), now.plusDays(1), true);
		saveDeal(trim, "종료됨_테스트", now.minusDays(3), now.minusDays(1), true);
		saveDeal(trim, "시작전_테스트", now.plusDays(1), null, true);
		saveDeal(trim, "비공개_테스트", now.minusDays(1), null, false);

		assertThat(titles("/api/deals?type=TIME_SALE"))
				.contains("노출중_테스트")
				.doesNotContain("종료됨_테스트", "시작전_테스트", "비공개_테스트");
	}

	@Test
	void 날짜는_한국시간_오프셋으로_내려준다() throws Exception {
		saveDeal(trimOf("아반떼"), "시간대_테스트", OffsetDateTime.now().minusDays(1),
				OffsetDateTime.now().plusDays(2), true);

		String body = mvc.get().uri("/api/deals?type=TIME_SALE").exchange().getResponse()
				.getContentAsString(StandardCharsets.UTF_8);
		List<String> endsAt = JsonPath.read(body, "$[?(@.title == '시간대_테스트')].endsAt");
		assertThat(endsAt).singleElement().asString().endsWith("+09:00");
	}

	@Test
	void 트림이_비활성이면_특가도_숨긴다() throws Exception {
		VehicleTrim trim = trimOf("아반떼");
		saveDeal(trim, "비활성트림_테스트", OffsetDateTime.now().minusDays(1), null, true);
		em.createQuery("update VehicleTrim t set t.active = false where t.id = :id")
				.setParameter("id", trim.getId()).executeUpdate();

		assertThat(titles("/api/deals")).doesNotContain("비활성트림_테스트");
	}

	// ---------------------------------------------------------------- 즉시출고

	@Test
	void 즉시출고는_판매완료를_빼고_총대수를_준다() {
		assertThat(mvc.get().uri("/api/instant"))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.totalCount", v -> v.assertThat().asNumber().isEqualTo(9))
				.hasPathSatisfying("$.items[*].status", v -> v.assertThat().asArray().doesNotContain("SOLD"))
				.hasPathSatisfying("$.items[*].vehicle.modelName", v -> v.assertThat().asArray()
						.doesNotContain("그랑 콜레오스"));
	}

	@Test
	void 즉시출고는_브랜드_국산수입_차종_연료로_거를_수_있다() {
		assertThat(mvc.get().uri("/api/instant?fuel=EV"))
				.bodyJson().extractingPath("$.items[*].vehicle.modelName").asArray()
				.containsExactlyInAnyOrder("아이오닉 5", "EV3");

		assertThat(mvc.get().uri("/api/instant?bodyType=VAN"))
				.bodyJson().extractingPath("$.totalCount").asNumber().isEqualTo(1);

		assertThat(mvc.get().uri("/api/instant?origin=IMPORTED"))
				.bodyJson().extractingPath("$.items[*].vehicle.brandName").asArray()
				.containsOnly("BMW", "벤츠");

		assertThat(mvc.get().uri("/api/instant?brandId={id}", brandId("기아")))
				.bodyJson().extractingPath("$.totalCount").asNumber().isEqualTo(4);
	}

	// ---------------------------------------------------------------- 배너·사이트

	@Test
	void 배너와_사이트_정보를_준다() {
		assertThat(mvc.get().uri("/api/banners"))
				.hasStatusOk()
				.bodyJson().extractingPath("$[*].linkUrl").asArray()
				.containsExactly("/deals?type=time", "/deals?type=nodeposit", "/instant");

		assertThat(mvc.get().uri("/api/site"))
				.hasStatusOk()
				.bodyJson()
				.hasPathSatisfying("$.name", v -> v.assertThat().isEqualTo("렌트DB(가칭)"))
				.hasPathSatisfying("$.phone", v -> v.assertThat().isNotNull());
	}

	// ---------------------------------------------------------------- helpers

	private Long brandId(String name) {
		return em.createQuery("select b from Brand b where b.name = :name", Brand.class)
				.setParameter("name", name).getSingleResult().getId();
	}

	private VehicleModel model(String name) {
		return em.createQuery("select m from VehicleModel m where m.name = :name", VehicleModel.class)
				.setParameter("name", name).getSingleResult();
	}

	private VehicleTrim trimOf(String modelName) {
		return em.createQuery("select t from VehicleTrim t where t.model.name = :name order by t.sortOrder",
				VehicleTrim.class).setParameter("name", modelName).setMaxResults(1).getSingleResult();
	}

	private void saveDeal(VehicleTrim trim, String title, OffsetDateTime startsAt, OffsetDateTime endsAt,
			boolean published) {
		em.persist(Deal.builder()
				.type(DealType.TIME_SALE)
				.trim(trim)
				.title(title)
				.monthlyPrice(300000)
				.periodMonths(48)
				.prepayRate(30)
				.startsAt(startsAt)
				.endsAt(endsAt)
				.published(published)
				.build());
		em.flush();
	}

	private List<Object> titles(String uri) throws Exception {
		String body = mvc.get().uri(uri).exchange().getResponse().getContentAsString(StandardCharsets.UTF_8);
		return JsonPath.read(body, "$[*].title");
	}

}
