package com.rentdb.banner.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.banner.domain.Banner;
import com.rentdb.banner.dto.admin.AdminBannerDtos.BannerRequest;
import com.rentdb.banner.dto.admin.AdminBannerDtos.BannerResponse;
import com.rentdb.banner.repository.BannerRepository;
import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBannerService {

	private final BannerRepository bannerRepository;
	private final Clock clock;

	public List<BannerResponse> getBanners() {
		OffsetDateTime now = OffsetDateTime.now(clock);
		return bannerRepository.findAllByOrderBySortOrderAscIdAsc().stream()
				.map(banner -> BannerResponse.of(banner, now))
				.toList();
	}

	public BannerResponse getBanner(Long bannerId) {
		return BannerResponse.of(findBanner(bannerId), OffsetDateTime.now(clock));
	}

	@Transactional
	public BannerResponse create(BannerRequest request) {
		validate(request);
		Banner banner = Banner.builder()
				.title(request.title().strip())
				.imagePcUrl(request.imagePcUrl().strip())
				.imageMobileUrl(request.imageMobileUrl().strip())
				.startsAt(request.startsAt())
				.build();
		apply(banner, request);
		return BannerResponse.of(bannerRepository.save(banner), OffsetDateTime.now(clock));
	}

	@Transactional
	public BannerResponse update(Long bannerId, BannerRequest request) {
		validate(request);
		Banner banner = findBanner(bannerId);
		apply(banner, request);
		return BannerResponse.of(banner, OffsetDateTime.now(clock));
	}

	@Transactional
	public void delete(Long bannerId) {
		bannerRepository.delete(findBanner(bannerId));
	}

	private void apply(Banner banner, BannerRequest request) {
		banner.update(request.title().strip(), request.imagePcUrl().strip(), request.imageMobileUrl().strip(),
				blankToNull(request.linkUrl()), request.startsAt(), request.endsAt(), request.sortOrder(),
				request.published());
	}

	private void validate(BannerRequest request) {
		if (request.endsAt() != null && !request.endsAt().isAfter(request.startsAt())) {
			throw new BusinessException(ErrorCode.VALIDATION_FAILED, "노출 종료 일시는 시작 일시보다 뒤여야 합니다.");
		}
	}

	private Banner findBanner(Long bannerId) {
		return bannerRepository.findById(bannerId).orElseThrow(() -> BusinessException.notFound("배너"));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
