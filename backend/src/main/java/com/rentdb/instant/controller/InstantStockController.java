package com.rentdb.instant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.instant.dto.InstantStockListResponse;
import com.rentdb.instant.service.InstantStockQueryService;
import com.rentdb.vehicle.domain.BodyType;
import com.rentdb.vehicle.domain.FuelType;
import com.rentdb.vehicle.domain.Origin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "즉시출고")
@RestController
@RequestMapping("/api/instant")
@RequiredArgsConstructor
public class InstantStockController {

	private final InstantStockQueryService instantStockQueryService;

	@Operation(summary = "노출 중인 즉시출고 차량", description = "판매완료는 제외, 필터는 모두 선택 사항 (전기차 필터는 fuel=EV)")
	@GetMapping
	public InstantStockListResponse instant(
			@RequestParam(required = false) Long brandId,
			@RequestParam(required = false) Origin origin,
			@RequestParam(required = false) BodyType bodyType,
			@RequestParam(required = false) FuelType fuel) {
		return instantStockQueryService.getVisibleStocks(brandId, origin, bodyType, fuel);
	}

}
