package com.rentdb.banner.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.banner.dto.admin.AdminBannerDtos.BannerRequest;
import com.rentdb.banner.dto.admin.AdminBannerDtos.BannerResponse;
import com.rentdb.banner.service.AdminBannerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 - 배너")
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

	private final AdminBannerService service;

	@Operation(summary = "배너 목록 (비공개·종료 포함, 노출 상태 계산)")
	@GetMapping
	public List<BannerResponse> banners() {
		return service.getBanners();
	}

	@Operation(summary = "배너 상세")
	@GetMapping("/{bannerId}")
	public BannerResponse banner(@PathVariable Long bannerId) {
		return service.getBanner(bannerId);
	}

	@Operation(summary = "배너 등록")
	@PostMapping
	public ResponseEntity<BannerResponse> create(@Valid @RequestBody BannerRequest request) {
		return ResponseEntity.status(201).body(service.create(request));
	}

	@Operation(summary = "배너 수정")
	@PutMapping("/{bannerId}")
	public BannerResponse update(@PathVariable Long bannerId, @Valid @RequestBody BannerRequest request) {
		return service.update(bannerId, request);
	}

	@Operation(summary = "배너 삭제")
	@DeleteMapping("/{bannerId}")
	public ResponseEntity<Void> delete(@PathVariable Long bannerId) {
		service.delete(bannerId);
		return ResponseEntity.noContent().build();
	}

}
