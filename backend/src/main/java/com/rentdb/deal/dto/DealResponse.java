package com.rentdb.deal.dto;

import java.time.OffsetDateTime;

import com.rentdb.deal.domain.Deal;
import com.rentdb.deal.domain.DealType;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.vehicle.dto.VehicleSummary;

public record DealResponse(
		Long id,
		DealType type,
		String title,
		String badge,
		/** 정가 월납입료 (취소선), 없으면 null */
		Long originalMonthly,
		long monthlyPrice,
		Long leaseMonthly,
		int periodMonths,
		int depositRate,
		int prepayRate,
		/** 종료 일시, 상시 특가면 null */
		OffsetDateTime endsAt,
		VehicleSummary vehicle) {

	public static DealResponse from(Deal deal) {
		return new DealResponse(
				deal.getId(),
				deal.getType(),
				deal.getTitle(),
				deal.getBadge(),
				deal.getOriginalMonthly(),
				deal.getMonthlyPrice(),
				deal.getLeaseMonthly(),
				deal.getPeriodMonths(),
				deal.getDepositRate(),
				deal.getPrepayRate(),
				TimeConfig.toKst(deal.getEndsAt()),
				VehicleSummary.from(deal.getTrim()));
	}

}
