package com.rentdb.vehicle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentdb.vehicle.domain.VehicleTrim;

public interface VehicleTrimRepository extends JpaRepository<VehicleTrim, Long> {

	List<VehicleTrim> findByModelIdAndActiveTrueOrderBySortOrderAscIdAsc(Long modelId);

	@EntityGraph(attributePaths = { "model", "model.brand" })
	Optional<VehicleTrim> findWithModelById(Long id);

	// 관리자: 비활성 포함
	List<VehicleTrim> findByModelIdOrderBySortOrderAscIdAsc(Long modelId);

	/** 브랜드 내 모델별 최저 트림가 — [modelId, minPrice] */
	@Query("""
			select t.model.id, min(t.price)
			from VehicleTrim t
			where t.model.brand.id = :brandId and t.active = true
			group by t.model.id
			""")
	List<Object[]> findMinPriceByBrandId(@Param("brandId") Long brandId);

}
