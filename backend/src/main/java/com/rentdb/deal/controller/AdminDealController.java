package com.rentdb.deal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.deal.domain.DealType;
import com.rentdb.deal.dto.admin.AdminDealDtos.DealRequest;
import com.rentdb.deal.dto.admin.AdminDealDtos.DealResponse;
import com.rentdb.deal.service.AdminDealService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 - 특가")
@RestController
@RequestMapping("/api/admin/deals")
@RequiredArgsConstructor
public class AdminDealController {

	private final AdminDealService service;

	@Operation(summary = "특가 목록 (비공개·종료 포함, 노출 상태 계산)")
	@GetMapping
	public List<DealResponse> deals(@RequestParam(required = false) DealType type) {
		return service.getDeals(type);
	}

	@Operation(summary = "특가 상세")
	@GetMapping("/{dealId}")
	public DealResponse deal(@PathVariable Long dealId) {
		return service.getDeal(dealId);
	}

	@Operation(summary = "특가 등록")
	@PostMapping
	public ResponseEntity<DealResponse> create(@Valid @RequestBody DealRequest request) {
		return ResponseEntity.status(201).body(service.create(request));
	}

	@Operation(summary = "특가 수정")
	@PutMapping("/{dealId}")
	public DealResponse update(@PathVariable Long dealId, @Valid @RequestBody DealRequest request) {
		return service.update(dealId, request);
	}

	@Operation(summary = "특가 삭제")
	@DeleteMapping("/{dealId}")
	public ResponseEntity<Void> delete(@PathVariable Long dealId) {
		service.delete(dealId);
		return ResponseEntity.noContent().build();
	}

}
