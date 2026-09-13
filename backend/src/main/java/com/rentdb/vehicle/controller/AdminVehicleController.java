package com.rentdb.vehicle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.BrandRequest;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.BrandResponse;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.ColorRequest;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.ColorResponse;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.ModelDetailResponse;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.ModelRequest;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.ModelResponse;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.OptionRequest;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.OptionResponse;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.TrimRequest;
import com.rentdb.vehicle.dto.admin.AdminVehicleDtos.TrimResponse;
import com.rentdb.vehicle.service.AdminVehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** 관리자 차량 관리 — 삭제 API 없음 (active=false 로 숨김) */
@Tag(name = "관리자 - 차량")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminVehicleController {

	private final AdminVehicleService service;

	@Operation(summary = "브랜드 목록 (비활성 포함)")
	@GetMapping("/brands")
	public List<BrandResponse> brands() {
		return service.getBrands();
	}

	@Operation(summary = "브랜드 등록")
	@PostMapping("/brands")
	public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
		return ResponseEntity.status(201).body(service.createBrand(request));
	}

	@Operation(summary = "브랜드 수정")
	@PutMapping("/brands/{brandId}")
	public BrandResponse updateBrand(@PathVariable Long brandId, @Valid @RequestBody BrandRequest request) {
		return service.updateBrand(brandId, request);
	}

	@Operation(summary = "브랜드의 모델 목록 (비활성 포함)")
	@GetMapping("/brands/{brandId}/models")
	public List<ModelResponse> models(@PathVariable Long brandId) {
		return service.getModels(brandId);
	}

	@Operation(summary = "모델 등록")
	@PostMapping("/models")
	public ResponseEntity<ModelResponse> createModel(@Valid @RequestBody ModelRequest request) {
		return ResponseEntity.status(201).body(service.createModel(request));
	}

	@Operation(summary = "모델 상세 — 트림·옵션·색상 (비활성 포함)")
	@GetMapping("/models/{modelId}")
	public ModelDetailResponse model(@PathVariable Long modelId) {
		return service.getModel(modelId);
	}

	@Operation(summary = "모델 수정")
	@PutMapping("/models/{modelId}")
	public ModelResponse updateModel(@PathVariable Long modelId, @Valid @RequestBody ModelRequest request) {
		return service.updateModel(modelId, request);
	}

	@Operation(summary = "세부모델(트림) 등록")
	@PostMapping("/models/{modelId}/trims")
	public ResponseEntity<TrimResponse> createTrim(@PathVariable Long modelId, @Valid @RequestBody TrimRequest request) {
		return ResponseEntity.status(201).body(service.createTrim(modelId, request));
	}

	@Operation(summary = "세부모델(트림) 수정")
	@PutMapping("/trims/{trimId}")
	public TrimResponse updateTrim(@PathVariable Long trimId, @Valid @RequestBody TrimRequest request) {
		return service.updateTrim(trimId, request);
	}

	@Operation(summary = "옵션 등록")
	@PostMapping("/models/{modelId}/options")
	public ResponseEntity<OptionResponse> createOption(@PathVariable Long modelId,
			@Valid @RequestBody OptionRequest request) {
		return ResponseEntity.status(201).body(service.createOption(modelId, request));
	}

	@Operation(summary = "옵션 수정")
	@PutMapping("/options/{optionId}")
	public OptionResponse updateOption(@PathVariable Long optionId, @Valid @RequestBody OptionRequest request) {
		return service.updateOption(optionId, request);
	}

	@Operation(summary = "색상 등록")
	@PostMapping("/models/{modelId}/colors")
	public ResponseEntity<ColorResponse> createColor(@PathVariable Long modelId,
			@Valid @RequestBody ColorRequest request) {
		return ResponseEntity.status(201).body(service.createColor(modelId, request));
	}

	@Operation(summary = "색상 수정")
	@PutMapping("/colors/{colorId}")
	public ColorResponse updateColor(@PathVariable Long colorId, @Valid @RequestBody ColorRequest request) {
		return service.updateColor(colorId, request);
	}

}
