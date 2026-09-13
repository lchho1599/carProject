package com.rentdb.instant.dto.admin;

import java.time.OffsetDateTime;

import com.rentdb.global.config.TimeConfig;
import com.rentdb.instant.domain.InstantStock;
import com.rentdb.instant.domain.StockStatus;
import com.rentdb.vehicle.dto.VehicleSummary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AdminInstantDtos {

	private AdminInstantDtos() {
	}

	public record InstantRequest(
			@NotNull(message = "차량(세부모델)을 선택해 주세요.") Long trimId,
			@NotBlank(message = "외장색을 입력해 주세요.") @Size(max = 100) String exteriorColor,
			@Size(max = 100) String interiorColor,
			@Size(max = 500) String optionsText,
			@NotNull(message = "차량가를 입력해 주세요.") @Min(0) Long vehiclePrice,
			@NotNull(message = "월 납입료를 입력해 주세요.") @Min(0) Long monthlyPrice,
			@NotBlank(message = "조건 문구를 입력해 주세요. (예: 48개월 / 선납금 30% 기준)") @Size(max = 100) String conditionText,
			@Size(max = 30) String badge,
			@NotNull(message = "재고 상태를 선택해 주세요.") StockStatus status,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean published) {
	}

	public record InstantResponse(
			Long id,
			String exteriorColor,
			String interiorColor,
			String optionsText,
			long vehiclePrice,
			long monthlyPrice,
			String conditionText,
			String badge,
			StockStatus status,
			int sortOrder,
			boolean published,
			/** 사용자 화면에 보이는지: 공개 + 판매완료 아님 + 차량 활성 */
			boolean visible,
			boolean vehicleActive,
			OffsetDateTime updatedAt,
			VehicleSummary vehicle) {

		public static InstantResponse from(InstantStock stock) {
			return new InstantResponse(stock.getId(), stock.getExteriorColor(), stock.getInteriorColor(),
					stock.getOptionsText(), stock.getVehiclePrice(), stock.getMonthlyPrice(), stock.getConditionText(),
					stock.getBadge(), stock.getStatus(), stock.getSortOrder(), stock.isPublished(), stock.isVisible(),
					stock.getTrim().isAvailable(), TimeConfig.toKst(stock.getUpdatedAt()),
					VehicleSummary.from(stock.getTrim()));
		}
	}

}
