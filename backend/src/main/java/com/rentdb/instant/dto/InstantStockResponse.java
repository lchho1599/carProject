package com.rentdb.instant.dto;

import com.rentdb.instant.domain.InstantStock;
import com.rentdb.instant.domain.StockStatus;
import com.rentdb.vehicle.dto.VehicleSummary;

public record InstantStockResponse(
		Long id,
		String exteriorColor,
		String interiorColor,
		String optionsText,
		long vehiclePrice,
		long monthlyPrice,
		String conditionText,
		String badge,
		StockStatus status,
		VehicleSummary vehicle) {

	public static InstantStockResponse from(InstantStock stock) {
		return new InstantStockResponse(
				stock.getId(),
				stock.getExteriorColor(),
				stock.getInteriorColor(),
				stock.getOptionsText(),
				stock.getVehiclePrice(),
				stock.getMonthlyPrice(),
				stock.getConditionText(),
				stock.getBadge(),
				stock.getStatus(),
				VehicleSummary.from(stock.getTrim()));
	}

}
