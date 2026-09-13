package com.rentdb.vehicle.dto;

import com.rentdb.vehicle.domain.BodyType;
import com.rentdb.vehicle.domain.FuelType;
import com.rentdb.vehicle.domain.VehicleModel;

/** 간편견적 1단계 모델 카드 */
public record ModelSummaryResponse(
		Long id,
		String name,
		String segment,
		BodyType bodyType,
		FuelType fuel,
		String imageUrl,
		/** 활성 트림 중 최저가, 트림이 없으면 null */
		Long minPrice) {

	public static ModelSummaryResponse of(VehicleModel model, Long minPrice) {
		return new ModelSummaryResponse(model.getId(), model.getName(), model.getSegment(), model.getBodyType(),
				model.getFuel(), model.getImageUrl(), minPrice);
	}

}
