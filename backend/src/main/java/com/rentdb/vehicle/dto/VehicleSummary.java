package com.rentdb.vehicle.dto;

import com.rentdb.vehicle.domain.BodyType;
import com.rentdb.vehicle.domain.FuelType;
import com.rentdb.vehicle.domain.VehicleModel;
import com.rentdb.vehicle.domain.VehicleTrim;

/** 상품 카드에 함께 내려주는 차량 요약 (브랜드·모델·트림) */
public record VehicleSummary(
		Long brandId,
		String brandName,
		Long modelId,
		String modelName,
		BodyType bodyType,
		FuelType fuel,
		String imageUrl,
		Long trimId,
		String trimName,
		long trimPrice) {

	/** trim → model → brand가 fetch join으로 로딩돼 있어야 한다. */
	public static VehicleSummary from(VehicleTrim trim) {
		VehicleModel model = trim.getModel();
		return new VehicleSummary(
				model.getBrand().getId(),
				model.getBrand().getName(),
				model.getId(),
				model.getName(),
				model.getBodyType(),
				model.getFuel(),
				model.getImageUrl(),
				trim.getId(),
				trim.getName(),
				trim.getPrice());
	}

}
