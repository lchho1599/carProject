package com.rentdb.banner.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.banner.dto.BannerResponse;
import com.rentdb.banner.repository.BannerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerQueryService {

	private final BannerRepository bannerRepository;
	private final Clock clock;

	public List<BannerResponse> getVisibleBanners() {
		return bannerRepository.findVisible(OffsetDateTime.now(clock)).stream()
				.map(BannerResponse::from)
				.toList();
	}

}
