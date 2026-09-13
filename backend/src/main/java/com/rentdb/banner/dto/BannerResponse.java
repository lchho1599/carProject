package com.rentdb.banner.dto;

import com.rentdb.banner.domain.Banner;

public record BannerResponse(Long id, String title, String imagePcUrl, String imageMobileUrl, String linkUrl) {

	public static BannerResponse from(Banner banner) {
		return new BannerResponse(banner.getId(), banner.getTitle(), banner.getImagePcUrl(),
				banner.getImageMobileUrl(), banner.getLinkUrl());
	}

}
