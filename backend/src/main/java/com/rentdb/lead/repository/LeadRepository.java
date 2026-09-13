package com.rentdb.lead.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentdb.lead.domain.Lead;

/**
 * 리드 저장소 — 단순 조회는 Spring Data 메서드, 동적 검색은 LeadRepositoryCustom(QueryDSL).
 */
public interface LeadRepository extends JpaRepository<Lead, Long>, LeadRepositoryCustom {

	boolean existsByPhoneAndCreatedAtAfter(String phone, OffsetDateTime after);

	/**
	 * 같은 연락처의 동시 신청(더블 클릭 등)을 줄 세운다 — 트랜잭션 종료 시 자동 해제되는 PostgreSQL 잠금.
	 */
	@Query(value = "select 1 from (select pg_advisory_xact_lock(hashtext(:phone))) as locked", nativeQuery = true)
	Integer lockByPhone(@Param("phone") String phone);

	long countByCreatedAtGreaterThanEqual(OffsetDateTime from);

	/** 상태별 건수 — [LeadStatus, Long] */
	@Query("select l.status, count(l) from Lead l group by l.status")
	List<Object[]> countGroupByStatus();

	List<Lead> findTop10ByOrderByCreatedAtDescIdDesc();

}
