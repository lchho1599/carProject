package com.rentdb.banner.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.banner.dto.BannerResponse;
import com.rentdb.banner.service.BannerQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "배너")
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

	private final BannerQueryService bannerQueryService;

	@Operation(summary = "노출 중인 메인 배너")
	@GetMapping
	public List<BannerResponse> banners() {
		return bannerQueryService.getVisibleBanners();
	}

}
