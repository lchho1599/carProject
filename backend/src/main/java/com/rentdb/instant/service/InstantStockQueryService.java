package com.rentdb.instant.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.instant.dto.InstantStockListResponse;
import com.rentdb.instant.dto.InstantStockResponse;
import com.rentdb.instant.repository.InstantStockRepository;
import com.rentdb.vehicle.domain.BodyType;
import com.rentdb.vehicle.domain.FuelType;
import com.rentdb.vehicle.domain.Origin;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstantStockQueryService {

	private final InstantStockRepository instantStockRepository;

	public InstantStockListResponse getVisibleStocks(Long brandId, Origin origin, BodyType bodyType, FuelType fuel) {
		return InstantStockListResponse.of(
				instantStockRepository.findVisible(brandId, origin, bodyType, fuel).stream()
						.map(InstantStockResponse::from)
						.toList());
	}

}
