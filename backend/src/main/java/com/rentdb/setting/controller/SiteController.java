package com.rentdb.setting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.setting.SiteProperties;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "사이트")
@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class SiteController {

	private final SiteProperties siteProperties;

	@Operation(summary = "사이트 공개 정보 (사이트명, 대표번호, 사업자 정보)")
	@GetMapping
	public SiteProperties site() {
		return siteProperties;
	}

}
