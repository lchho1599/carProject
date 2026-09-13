package com.rentdb.instant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentdb.instant.domain.InstantStock;
import com.rentdb.vehicle.domain.BodyType;
import com.rentdb.vehicle.domain.FuelType;
import com.rentdb.vehicle.domain.Origin;

public interface InstantStockRepository extends JpaRepository<InstantStock, Long> {

	/** 노출 중인 즉시출고: 공개 + 판매완료 제외 + 차량 활성, 조건은 값이 있을 때만 적용 */
	@Query("""
			select s from InstantStock s
			join fetch s.trim t
			join fetch t.model m
			join fetch m.brand b
			where s.published = true
			  and s.status <> com.rentdb.instant.domain.StockStatus.SOLD
			  and t.active = true and m.active = true and b.active = true
			  and (:brandId is null or b.id = :brandId)
			  and (:origin is null or b.origin = :origin)
			  and (:bodyType is null or m.bodyType = :bodyType)
			  and (:fuel is null or m.fuel = :fuel)
			order by s.sortOrder, s.id
			""")
	List<InstantStock> findVisible(
			@Param("brandId") Long brandId,
			@Param("origin") Origin origin,
			@Param("bodyType") BodyType bodyType,
			@Param("fuel") FuelType fuel);

	@EntityGraph(attributePaths = { "trim", "trim.model", "trim.model.brand" })
	Optional<InstantStock> findWithVehicleById(Long id);

	// 관리자: 비공개·판매완료 포함
	@EntityGraph(attributePaths = { "trim", "trim.model", "trim.model.brand" })
	List<InstantStock> findAllByOrderBySortOrderAscIdAsc();

	@EntityGraph(attributePaths = { "trim", "trim.model", "trim.model.brand" })
	List<InstantStock> findByStatusOrderBySortOrderAscIdAsc(com.rentdb.instant.domain.StockStatus status);

}
