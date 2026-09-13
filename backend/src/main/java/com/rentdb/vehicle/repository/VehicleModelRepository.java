package com.rentdb.vehicle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.vehicle.domain.VehicleModel;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

	List<VehicleModel> findByBrandIdAndActiveTrueOrderBySortOrderAscIdAsc(Long brandId);

	@EntityGraph(attributePaths = "brand")
	Optional<VehicleModel> findWithBrandByIdAndActiveTrue(Long id);

	// 관리자: 비활성 포함
	@EntityGraph(attributePaths = "brand")
	List<VehicleModel> findByBrandIdOrderBySortOrderAscIdAsc(Long brandId);

	@EntityGraph(attributePaths = "brand")
	Optional<VehicleModel> findWithBrandById(Long id);

	boolean existsByBrandIdAndName(Long brandId, String name);

	boolean existsByBrandIdAndNameAndIdNot(Long brandId, String name, Long id);

}
