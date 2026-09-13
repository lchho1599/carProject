package com.rentdb.deal.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentdb.deal.domain.Deal;
import com.rentdb.deal.domain.DealType;

public interface DealRepository extends JpaRepository<Deal, Long> {

	/** 노출 중인 특가: 공개 + 노출 기간 안 + 차량(트림·모델·브랜드) 활성 */
	@Query("""
			select d from Deal d
			join fetch d.trim t
			join fetch t.model m
			join fetch m.brand b
			where d.published = true
			  and d.startsAt <= :now
			  and (d.endsAt is null or d.endsAt > :now)
			  and (:type is null or d.type = :type)
			  and t.active = true and m.active = true and b.active = true
			order by d.type, d.sortOrder, d.id
			""")
	List<Deal> findVisible(@Param("type") DealType type, @Param("now") OffsetDateTime now);

	@EntityGraph(attributePaths = { "trim", "trim.model", "trim.model.brand" })
	Optional<Deal> findWithVehicleById(Long id);

	// 관리자: 비공개·종료 포함
	@EntityGraph(attributePaths = { "trim", "trim.model", "trim.model.brand" })
	List<Deal> findAllByOrderByTypeAscSortOrderAscIdAsc();

	@EntityGraph(attributePaths = { "trim", "trim.model", "trim.model.brand" })
	List<Deal> findByTypeOrderBySortOrderAscIdAsc(DealType type);

}
