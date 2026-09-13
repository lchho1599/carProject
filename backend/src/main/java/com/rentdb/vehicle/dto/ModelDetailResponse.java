package com.rentdb.vehicle.dto;

import java.util.List;

import com.rentdb.vehicle.domain.BodyType;
import com.rentdb.vehicle.domain.FuelType;
import com.rentdb.vehicle.domain.ModelColor;
import com.rentdb.vehicle.domain.ModelOption;
import com.rentdb.vehicle.domain.VehicleModel;
import com.rentdb.vehicle.domain.VehicleTrim;

/** 간편견적 2단계 — 모델 + 트림·옵션·색상 */
public record ModelDetailResponse(
		Long id,
		BrandResponse brand,
		String name,
		String segment,
		BodyType bodyType,
		FuelType fuel,
		String imageUrl,
		List<TrimItem> trims,
		List<OptionItem> options,
		List<ColorItem> colors) {

	public record TrimItem(Long id, String name, long price) {
		static TrimItem from(VehicleTrim trim) {
			return new TrimItem(trim.getId(), trim.getName(), trim.getPrice());
		}
	}

	public record OptionItem(Long id, String name, long price) {
		static OptionItem from(ModelOption option) {
			return new OptionItem(option.getId(), option.getName(), option.getPrice());
		}
	}

	public record ColorItem(Long id, String name, String hexCode, long extraPrice) {
		static ColorItem from(ModelColor color) {
			return new ColorItem(color.getId(), color.getName(), color.getHexCode(), color.getExtraPrice());
		}
	}

	public static ModelDetailResponse of(VehicleModel model, List<VehicleTrim> trims, List<ModelOption> options,
			List<ModelColor> colors) {
		return new ModelDetailResponse(
				model.getId(),
				BrandResponse.from(model.getBrand()),
				model.getName(),
				model.getSegment(),
				model.getBodyType(),
				model.getFuel(),
				model.getImageUrl(),
				trims.stream().map(TrimItem::from).toList(),
				options.stream().map(OptionItem::from).toList(),
				colors.stream().map(ColorItem::from).toList());
	}

}
