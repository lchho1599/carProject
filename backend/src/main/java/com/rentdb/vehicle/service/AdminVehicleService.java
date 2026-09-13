package com.rentdb.vehicle.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;
import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.ModelColor;
import com.rentdb.vehicle.domain.ModelOption;
import com.rentdb.vehicle.domain.VehicleModel;
import com.rentdb.vehicle.domain.VehicleTrim;
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
import com.rentdb.vehicle.repository.BrandRepository;
import com.rentdb.vehicle.repository.ModelColorRepository;
import com.rentdb.vehicle.repository.ModelOptionRepository;
import com.rentdb.vehicle.repository.VehicleModelRepository;
import com.rentdb.vehicle.repository.VehicleTrimRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 차량 마스터 관리 — 삭제 대신 active=false 로 숨긴다 (리드·특가·재고가 참조하므로).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminVehicleService {

	private final BrandRepository brandRepository;
	private final VehicleModelRepository modelRepository;
	private final VehicleTrimRepository trimRepository;
	private final ModelOptionRepository optionRepository;
	private final ModelColorRepository colorRepository;

	// ---------------------------------------------------------------- 브랜드

	public List<BrandResponse> getBrands() {
		return brandRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(BrandResponse::from).toList();
	}

	@Transactional
	public BrandResponse createBrand(BrandRequest request) {
		String name = request.name().strip();
		if (brandRepository.existsByName(name)) {
			throw duplicated("이미 등록된 브랜드명입니다.");
		}
		Brand brand = Brand.builder()
				.name(name)
				.origin(request.origin())
				.logoUrl(blankToNull(request.logoUrl()))
				.sortOrder(request.sortOrder())
				.build();
		brand.update(name, request.origin(), blankToNull(request.logoUrl()), request.sortOrder(), request.active());
		return BrandResponse.from(brandRepository.save(brand));
	}

	@Transactional
	public BrandResponse updateBrand(Long brandId, BrandRequest request) {
		Brand brand = brandRepository.findById(brandId).orElseThrow(() -> BusinessException.notFound("브랜드"));
		String name = request.name().strip();
		if (brandRepository.existsByNameAndIdNot(name, brandId)) {
			throw duplicated("이미 등록된 브랜드명입니다.");
		}
		brand.update(name, request.origin(), blankToNull(request.logoUrl()), request.sortOrder(), request.active());
		return BrandResponse.from(brand);
	}

	// ---------------------------------------------------------------- 모델

	public List<ModelResponse> getModels(Long brandId) {
		findBrand(brandId);
		return modelRepository.findByBrandIdOrderBySortOrderAscIdAsc(brandId).stream().map(ModelResponse::from).toList();
	}

	public ModelDetailResponse getModel(Long modelId) {
		VehicleModel model = findModel(modelId);
		return new ModelDetailResponse(
				ModelResponse.from(model),
				trimRepository.findByModelIdOrderBySortOrderAscIdAsc(modelId).stream().map(TrimResponse::from).toList(),
				optionRepository.findByModelIdOrderBySortOrderAscIdAsc(modelId).stream().map(OptionResponse::from).toList(),
				colorRepository.findByModelIdOrderBySortOrderAscIdAsc(modelId).stream().map(ColorResponse::from).toList());
	}

	@Transactional
	public ModelResponse createModel(ModelRequest request) {
		Brand brand = findBrand(request.brandId());
		String name = request.name().strip();
		if (modelRepository.existsByBrandIdAndName(brand.getId(), name)) {
			throw duplicated("같은 브랜드에 이미 등록된 모델명입니다.");
		}
		VehicleModel model = VehicleModel.builder()
				.brand(brand)
				.name(name)
				.segment(blankToNull(request.segment()))
				.bodyType(request.bodyType())
				.fuel(request.fuel())
				.imageUrl(blankToNull(request.imageUrl()))
				.sortOrder(request.sortOrder())
				.build();
		model.update(name, blankToNull(request.segment()), request.bodyType(), request.fuel(),
				blankToNull(request.imageUrl()), request.sortOrder(), request.active());
		return ModelResponse.from(modelRepository.save(model));
	}

	/** 브랜드 이동은 허용하지 않는다 (기존 리드·특가의 브랜드 정보가 어긋나지 않도록) */
	@Transactional
	public ModelResponse updateModel(Long modelId, ModelRequest request) {
		VehicleModel model = findModel(modelId);
		if (!model.getBrand().getId().equals(request.brandId())) {
			throw new BusinessException(ErrorCode.VALIDATION_FAILED, "모델의 브랜드는 바꿀 수 없습니다. 새 모델로 등록해 주세요.");
		}
		String name = request.name().strip();
		if (modelRepository.existsByBrandIdAndNameAndIdNot(model.getBrand().getId(), name, modelId)) {
			throw duplicated("같은 브랜드에 이미 등록된 모델명입니다.");
		}
		model.update(name, blankToNull(request.segment()), request.bodyType(), request.fuel(),
				blankToNull(request.imageUrl()), request.sortOrder(), request.active());
		return ModelResponse.from(model);
	}

	// ---------------------------------------------------------------- 트림·옵션·색상

	@Transactional
	public TrimResponse createTrim(Long modelId, TrimRequest request) {
		VehicleTrim trim = VehicleTrim.builder()
				.model(findModel(modelId))
				.name(request.name().strip())
				.price(request.price())
				.sortOrder(request.sortOrder())
				.build();
		trim.update(request.name().strip(), request.price(), request.sortOrder(), request.active());
		return TrimResponse.from(trimRepository.save(trim));
	}

	@Transactional
	public TrimResponse updateTrim(Long trimId, TrimRequest request) {
		VehicleTrim trim = trimRepository.findById(trimId).orElseThrow(() -> BusinessException.notFound("세부모델"));
		trim.update(request.name().strip(), request.price(), request.sortOrder(), request.active());
		return TrimResponse.from(trim);
	}

	@Transactional
	public OptionResponse createOption(Long modelId, OptionRequest request) {
		ModelOption option = ModelOption.builder()
				.model(findModel(modelId))
				.name(request.name().strip())
				.price(request.price())
				.sortOrder(request.sortOrder())
				.build();
		option.update(request.name().strip(), request.price(), request.sortOrder(), request.active());
		return OptionResponse.from(optionRepository.save(option));
	}

	@Transactional
	public OptionResponse updateOption(Long optionId, OptionRequest request) {
		ModelOption option = optionRepository.findById(optionId).orElseThrow(() -> BusinessException.notFound("옵션"));
		option.update(request.name().strip(), request.price(), request.sortOrder(), request.active());
		return OptionResponse.from(option);
	}

	@Transactional
	public ColorResponse createColor(Long modelId, ColorRequest request) {
		ModelColor color = ModelColor.builder()
				.model(findModel(modelId))
				.name(request.name().strip())
				.hexCode(blankToNull(request.hexCode()))
				.extraPrice(request.extraPrice())
				.sortOrder(request.sortOrder())
				.build();
		color.update(request.name().strip(), normalizeHex(request.hexCode()), request.extraPrice(), request.sortOrder(),
				request.active());
		return ColorResponse.from(colorRepository.save(color));
	}

	@Transactional
	public ColorResponse updateColor(Long colorId, ColorRequest request) {
		ModelColor color = colorRepository.findById(colorId).orElseThrow(() -> BusinessException.notFound("색상"));
		color.update(request.name().strip(), normalizeHex(request.hexCode()), request.extraPrice(), request.sortOrder(),
				request.active());
		return ColorResponse.from(color);
	}

	// ---------------------------------------------------------------- helpers

	private Brand findBrand(Long brandId) {
		return brandRepository.findById(brandId).orElseThrow(() -> BusinessException.notFound("브랜드"));
	}

	private VehicleModel findModel(Long modelId) {
		return modelRepository.findWithBrandById(modelId).orElseThrow(() -> BusinessException.notFound("차량 모델"));
	}

	private static BusinessException duplicated(String message) {
		return new BusinessException(ErrorCode.DUPLICATED, message);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

	private static String normalizeHex(String hex) {
		String value = blankToNull(hex);
		return value == null ? null : value.toUpperCase(java.util.Locale.ROOT);
	}

}
