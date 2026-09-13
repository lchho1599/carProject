package com.rentdb.deal.dto.admin;

import java.time.OffsetDateTime;

import com.rentdb.deal.domain.Deal;
import com.rentdb.deal.domain.DealType;
import com.rentdb.global.common.DisplayStatus;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.vehicle.dto.VehicleSummary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AdminDealDtos {

	private AdminDealDtos() {
	}

	public record DealRequest(
			@NotNull(message = "특가 유형을 선택해 주세요.") DealType type,
			@NotNull(message = "차량(세부모델)을 선택해 주세요.") Long trimId,
			@NotBlank(message = "표시 제목을 입력해 주세요.") @Size(max = 200) String title,
			@Size(max = 30) String badge,
			@Min(0) Long originalMonthly,
			@NotNull(message = "월 납입료를 입력해 주세요.") @Min(0) Long monthlyPrice,
			@Min(0) Long leaseMonthly,
			@NotNull(message = "이용기간을 선택해 주세요.") Integer periodMonths,
			@Min(0) @Max(100) int depositRate,
			@Min(0) @Max(100) int prepayRate,
			@NotNull(message = "노출 시작 일시를 입력해 주세요.") OffsetDateTime startsAt,
			OffsetDateTime endsAt,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean published) {
	}

	public record DealResponse(
			Long id,
			DealType type,
			String title,
			String badge,
			Long originalMonthly,
			long monthlyPrice,
			Long leaseMonthly,
			int periodMonths,
			int depositRate,
			int prepayRate,
			OffsetDateTime startsAt,
			OffsetDateTime endsAt,
			int sortOrder,
			boolean published,
			DisplayStatus displayStatus,
			boolean vehicleActive,
			VehicleSummary vehicle) {

		/** trim → model → brand 가 로딩돼 있어야 한다 */
		public static DealResponse of(Deal deal, OffsetDateTime now) {
			boolean vehicleActive = deal.getTrim().isAvailable();
			DisplayStatus status = vehicleActive
					? DisplayStatus.of(deal.isPublished(), deal.getStartsAt(), deal.getEndsAt(), now)
					: DisplayStatus.HIDDEN;
			return new DealResponse(deal.getId(), deal.getType(), deal.getTitle(), deal.getBadge(),
					deal.getOriginalMonthly(), deal.getMonthlyPrice(), deal.getLeaseMonthly(), deal.getPeriodMonths(),
					deal.getDepositRate(), deal.getPrepayRate(), TimeConfig.toKst(deal.getStartsAt()),
					TimeConfig.toKst(deal.getEndsAt()), deal.getSortOrder(), deal.isPublished(), status, vehicleActive,
					VehicleSummary.from(deal.getTrim()));
		}
	}

}
