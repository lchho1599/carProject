package com.rentdb.banner.dto.admin;

import java.time.OffsetDateTime;

import com.rentdb.banner.domain.Banner;
import com.rentdb.global.common.DisplayStatus;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.global.validation.PublicUrl;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AdminBannerDtos {

	private AdminBannerDtos() {
	}

	public record BannerRequest(
			@NotBlank(message = "배너 제목을 입력해 주세요.") @Size(max = 200) String title,
			@NotBlank(message = "PC 이미지를 올려 주세요.") @PublicUrl @Size(max = 500) String imagePcUrl,
			@NotBlank(message = "모바일 이미지를 올려 주세요.") @PublicUrl @Size(max = 500) String imageMobileUrl,
			@PublicUrl @Size(max = 500) String linkUrl,
			@NotNull(message = "노출 시작 일시를 입력해 주세요.") OffsetDateTime startsAt,
			OffsetDateTime endsAt,
			@Min(0) @Max(9999) int sortOrder,
			@NotNull Boolean published) {
	}

	public record BannerResponse(
			Long id,
			String title,
			String imagePcUrl,
			String imageMobileUrl,
			String linkUrl,
			OffsetDateTime startsAt,
			OffsetDateTime endsAt,
			int sortOrder,
			boolean published,
			DisplayStatus displayStatus) {

		public static BannerResponse of(Banner banner, OffsetDateTime now) {
			return new BannerResponse(banner.getId(), banner.getTitle(), banner.getImagePcUrl(),
					banner.getImageMobileUrl(), banner.getLinkUrl(), TimeConfig.toKst(banner.getStartsAt()),
					TimeConfig.toKst(banner.getEndsAt()), banner.getSortOrder(), banner.isPublished(),
					DisplayStatus.of(banner.isPublished(), banner.getStartsAt(), banner.getEndsAt(), now));
		}
	}

}
