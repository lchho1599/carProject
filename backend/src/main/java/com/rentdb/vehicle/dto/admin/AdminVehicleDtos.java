package com.rentdb.vehicle.dto.admin;

import java.util.List;

import com.rentdb.global.validation.PublicUrl;
import com.rentdb.vehicle.domain.BodyType;
import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.FuelType;
import com.rentdb.vehicle.domain.ModelColor;
import com.rentdb.vehicle.domain.ModelOption;
import com.rentdb.vehicle.domain.Origin;
import com.rentdb.vehicle.domain.VehicleModel;
import com.rentdb.vehicle.domain.VehicleTrim;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 관리자 차량 관리 요청·응답 — 비활성(active=false) 데이터도 모두 포함한다.
 */
public final class AdminVehicleDtos {

	private AdminVehicleDtos() {
	}

	// ---------------------------------------------------------------- 브랜드

	public record BrandRequest(
			@NotBlank(message = "브랜드명을 입력해 주세요.") @Size(max = 50) String name,
			@NotNull(message = "국산·수입을 선택해 주세요.") Origin origin,
			@PublicUrl @Size(max = 500) String logoUrl,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean active) {
	}

	public record BrandResponse(Long id, String name, Origin origin, String logoUrl, int sortOrder, boolean active) {
		public static BrandResponse from(Brand brand) {
			return new BrandResponse(brand.getId(), brand.getName(), brand.getOrigin(), brand.getLogoUrl(),
					brand.getSortOrder(), brand.isActive());
		}
	}

	// ---------------------------------------------------------------- 모델

	public record ModelRequest(
			@NotNull(message = "브랜드를 선택해 주세요.") Long brandId,
			@NotBlank(message = "모델명을 입력해 주세요.") @Size(max = 100) String name,
			@Size(max = 30) String segment,
			@NotNull(message = "차종을 선택해 주세요.") BodyType bodyType,
			@NotNull(message = "연료를 선택해 주세요.") FuelType fuel,
			@PublicUrl @Size(max = 500) String imageUrl,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean active) {
	}

	public record ModelResponse(Long id, Long brandId, String brandName, String name, String segment,
			BodyType bodyType, FuelType fuel, String imageUrl, int sortOrder, boolean active) {
		public static ModelResponse from(VehicleModel model) {
			return new ModelResponse(model.getId(), model.getBrand().getId(), model.getBrand().getName(),
					model.getName(), model.getSegment(), model.getBodyType(), model.getFuel(), model.getImageUrl(),
					model.getSortOrder(), model.isActive());
		}
	}

	public record ModelDetailResponse(ModelResponse model, List<TrimResponse> trims, List<OptionResponse> options,
			List<ColorResponse> colors) {
	}

	// ---------------------------------------------------------------- 트림·옵션·색상

	public record TrimRequest(
			@NotBlank(message = "세부모델명을 입력해 주세요.") @Size(max = 200) String name,
			@NotNull(message = "가격을 입력해 주세요.") @Min(0) Long price,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean active) {
	}

	public record TrimResponse(Long id, String name, long price, int sortOrder, boolean active) {
		public static TrimResponse from(VehicleTrim trim) {
			return new TrimResponse(trim.getId(), trim.getName(), trim.getPrice(), trim.getSortOrder(), trim.isActive());
		}
	}

	public record OptionRequest(
			@NotBlank(message = "옵션명을 입력해 주세요.") @Size(max = 200) String name,
			@NotNull(message = "가격을 입력해 주세요.") @Min(0) Long price,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean active) {
	}

	public record OptionResponse(Long id, String name, long price, int sortOrder, boolean active) {
		public static OptionResponse from(ModelOption option) {
			return new OptionResponse(option.getId(), option.getName(), option.getPrice(), option.getSortOrder(),
					option.isActive());
		}
	}

	public record ColorRequest(
			@NotBlank(message = "색상명을 입력해 주세요.") @Size(max = 100) String name,
			@Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "색상 코드는 #RRGGBB 형식입니다.") String hexCode,
			@NotNull(message = "추가금을 입력해 주세요.") @Min(0) Long extraPrice,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean active) {
	}

	public record ColorResponse(Long id, String name, String hexCode, long extraPrice, int sortOrder, boolean active) {
		public static ColorResponse from(ModelColor color) {
			return new ColorResponse(color.getId(), color.getName(), color.getHexCode(), color.getExtraPrice(),
					color.getSortOrder(), color.isActive());
		}
	}

}
