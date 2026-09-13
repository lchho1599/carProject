package com.rentdb.vehicle.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.vehicle.domain.ModelOption;

public interface ModelOptionRepository extends JpaRepository<ModelOption, Long> {

	List<ModelOption> findByModelIdAndActiveTrueOrderBySortOrderAscIdAsc(Long modelId);

	List<ModelOption> findByIdInAndModelIdAndActiveTrueOrderBySortOrderAscIdAsc(Collection<Long> ids, Long modelId);

	// 관리자: 비활성 포함
	List<ModelOption> findByModelIdOrderBySortOrderAscIdAsc(Long modelId);

}
