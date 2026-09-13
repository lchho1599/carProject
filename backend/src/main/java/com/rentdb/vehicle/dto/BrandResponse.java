package com.rentdb.vehicle.dto;

import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.Origin;

public record BrandResponse(Long id, String name, Origin origin, String logoUrl) {

	public static BrandResponse from(Brand brand) {
		return new BrandResponse(brand.getId(), brand.getName(), brand.getOrigin(), brand.getLogoUrl());
	}

}
