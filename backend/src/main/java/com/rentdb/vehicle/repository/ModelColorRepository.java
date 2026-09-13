package com.rentdb.vehicle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.vehicle.domain.ModelColor;

public interface ModelColorRepository extends JpaRepository<ModelColor, Long> {

	List<ModelColor> findByModelIdAndActiveTrueOrderBySortOrderAscIdAsc(Long modelId);

	Optional<ModelColor> findByIdAndModelIdAndActiveTrue(Long id, Long modelId);

	// 관리자: 비활성 포함
	List<ModelColor> findByModelIdOrderBySortOrderAscIdAsc(Long modelId);

}
