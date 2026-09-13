package com.rentdb.vehicle.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.global.error.BusinessException;
import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.Origin;
import com.rentdb.vehicle.domain.VehicleModel;
import com.rentdb.vehicle.dto.BrandResponse;
import com.rentdb.vehicle.dto.ModelDetailResponse;
import com.rentdb.vehicle.dto.ModelSummaryResponse;
import com.rentdb.vehicle.repository.BrandRepository;
import com.rentdb.vehicle.repository.ModelColorRepository;
import com.rentdb.vehicle.repository.ModelOptionRepository;
import com.rentdb.vehicle.repository.VehicleModelRepository;
import com.rentdb.vehicle.repository.VehicleTrimRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 화면용 차량 조회 — 활성(is_active) 데이터만 노출한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleQueryService {

	private final BrandRepository brandRepository;
	private final VehicleModelRepository modelRepository;
	private final VehicleTrimRepository trimRepository;
	private final ModelOptionRepository optionRepository;
	private final ModelColorRepository colorRepository;

	public List<BrandResponse> getBrands(Origin origin) {
		List<Brand> brands = origin == null
				? brandRepository.findByActiveTrueOrderBySortOrderAscIdAsc()
				: brandRepository.findByOriginAndActiveTrueOrderBySortOrderAscIdAsc(origin);
		return brands.stream().map(BrandResponse::from).toList();
	}

	public List<ModelSummaryResponse> getModels(Long brandId) {
		brandRepository.findByIdAndActiveTrue(brandId)
				.orElseThrow(() -> BusinessException.notFound("브랜드"));

		Map<Long, Long> minPrices = trimRepository.findMinPriceByBrandId(brandId).stream()
				.collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

		return modelRepository.findByBrandIdAndActiveTrueOrderBySortOrderAscIdAsc(brandId).stream()
				.map(model -> ModelSummaryResponse.of(model, minPrices.get(model.getId())))
				.toList();
	}

	public ModelDetailResponse getModel(Long modelId) {
		VehicleModel model = modelRepository.findWithBrandByIdAndActiveTrue(modelId)
				.filter(m -> m.getBrand().isActive())
				.orElseThrow(() -> BusinessException.notFound("차량 모델"));

		return ModelDetailResponse.of(
				model,
				trimRepository.findByModelIdAndActiveTrueOrderBySortOrderAscIdAsc(modelId),
				optionRepository.findByModelIdAndActiveTrueOrderBySortOrderAscIdAsc(modelId),
				colorRepository.findByModelIdAndActiveTrueOrderBySortOrderAscIdAsc(modelId));
	}

}
