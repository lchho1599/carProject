package com.rentdb.vehicle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.Origin;

public interface BrandRepository extends JpaRepository<Brand, Long> {

	List<Brand> findByActiveTrueOrderBySortOrderAscIdAsc();

	List<Brand> findByOriginAndActiveTrueOrderBySortOrderAscIdAsc(Origin origin);

	Optional<Brand> findByIdAndActiveTrue(Long id);

	// 관리자: 비활성 포함
	List<Brand> findAllByOrderBySortOrderAscIdAsc();

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

}
