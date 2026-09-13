package com.rentdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.deal.domain.Deal;
import com.rentdb.deal.domain.DealType;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.domain.LeadType;
import com.rentdb.vehicle.domain.VehicleTrim;

import jakarta.persistence.EntityManager;

/**
 * Flyway 스키마 ↔ JPA 엔티티 매핑, 로컬 샘플 데이터, jsonb 읽기/쓰기를 확인한다.
 * 로컬 DB(docker compose, 5433)가 실행 중이어야 한다.
 */
@SpringBootTest
@Transactional
class SchemaAndSeedTest {

	@Autowired
	EntityManager em;

	@Test
	void 샘플_데이터가_설계한_규모로_들어있다() {
		assertThat(count("Brand")).isEqualTo(6);
		assertThat(count("VehicleModel")).isEqualTo(15);
		assertThat(count("VehicleTrim")).isEqualTo(30);
		assertThat(count("ModelOption")).isEqualTo(60);
		assertThat(count("ModelColor")).isEqualTo(60);
		assertThat(count("Deal")).isEqualTo(10);
		assertThat(count("InstantStock")).isEqualTo(10);
		assertThat(count("Banner")).isEqualTo(3);
		// 개발 중 화면에서 넣은 신청과 섞이지 않도록 샘플 연락처(0100000000x)만 센다
		assertThat(em.createQuery("select count(l) from Lead l where l.phone like '0100000000%'", Long.class)
				.getSingleResult()).isEqualTo(6);
		assertThat(count("AppSetting")).isEqualTo(1);
	}

	@Test
	void 특가는_트림_모델_브랜드로_연결된다() {
		Deal deal = em.createQuery(
				"select d from Deal d join fetch d.trim t join fetch t.model m join fetch m.brand "
						+ "where d.type = :type order by d.sortOrder", Deal.class)
				.setParameter("type", DealType.TIME_SALE)
				.setMaxResults(1)
				.getSingleResult();

		assertThat(deal.getTrim().getModel().getName()).isEqualTo("쏘렌토");
		assertThat(deal.getTrim().getModel().getBrand().getName()).isEqualTo("기아");
	}

	@Test
	void 리드의_jsonb_컬럼을_읽고_쓸_수_있다() {
		Lead seeded = em.createQuery("select l from Lead l where l.phone = '01000000001'", Lead.class)
				.getSingleResult();
		assertThat(seeded.getConditions()).containsEntry("periodMonths", 48);
		assertThat(seeded.getVehicleSnapshot()).containsEntry("model", "쏘렌토");
		assertThat(seeded.getUtm()).containsEntry("utm_source", "naver");

		VehicleTrim trim = em.createQuery("select t from VehicleTrim t order by t.id", VehicleTrim.class)
				.setMaxResults(1)
				.getSingleResult();
		Lead lead = Lead.builder()
				.type(LeadType.ESTIMATE)
				.name("저장테스트")
				.phone("01099999999")
				.trim(trim)
				.vehicleSnapshot(Map.of("trim", trim.getName(), "trimPrice", trim.getPrice()))
				.conditions(Map.of("useType", "RENT", "periodMonths", 36))
				.totalPrice(trim.getPrice())
				.agreePrivacy(true)
				.utm(Map.of("utm_source", "test"))
				.build();
		em.persist(lead);
		em.flush();
		em.clear();

		Lead found = em.find(Lead.class, lead.getId());
		assertThat(found.getConditions()).containsEntry("useType", "RENT").containsEntry("periodMonths", 36);
		assertThat(found.getCreatedAt()).isNotNull();
	}

	private long count(String entity) {
		return em.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult();
	}

}
