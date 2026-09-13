package com.rentdb.vehicle.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.vehicle.domain.Origin;
import com.rentdb.vehicle.dto.BrandResponse;
import com.rentdb.vehicle.dto.ModelDetailResponse;
import com.rentdb.vehicle.dto.ModelSummaryResponse;
import com.rentdb.vehicle.service.VehicleQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "차량")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VehicleController {

	private final VehicleQueryService vehicleQueryService;

	@Operation(summary = "브랜드 목록", description = "origin을 비우면 전체 (DOMESTIC=국산, IMPORTED=수입)")
	@GetMapping("/brands")
	public List<BrandResponse> brands(@RequestParam(required = false) Origin origin) {
		return vehicleQueryService.getBrands(origin);
	}

	@Operation(summary = "브랜드의 모델 목록 (간편견적 1단계)")
	@GetMapping("/brands/{brandId}/models")
	public List<ModelSummaryResponse> models(@PathVariable Long brandId) {
		return vehicleQueryService.getModels(brandId);
	}

	@Operation(summary = "모델 상세 — 트림·옵션·색상 (간편견적 2단계)")
	@GetMapping("/models/{modelId}")
	public ModelDetailResponse model(@PathVariable Long modelId) {
		return vehicleQueryService.getModel(modelId);
	}

}
