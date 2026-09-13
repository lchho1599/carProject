package com.rentdb.instant.controller;

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

import com.rentdb.instant.domain.StockStatus;
import com.rentdb.instant.dto.admin.AdminInstantDtos.InstantRequest;
import com.rentdb.instant.dto.admin.AdminInstantDtos.InstantResponse;
import com.rentdb.instant.service.AdminInstantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 - 즉시출고")
@RestController
@RequestMapping("/api/admin/instant")
@RequiredArgsConstructor
public class AdminInstantController {

	private final AdminInstantService service;

	@Operation(summary = "즉시출고 재고 목록 (비공개·판매완료 포함)")
	@GetMapping
	public List<InstantResponse> stocks(@RequestParam(required = false) StockStatus status) {
		return service.getStocks(status);
	}

	@Operation(summary = "즉시출고 재고 상세")
	@GetMapping("/{stockId}")
	public InstantResponse stock(@PathVariable Long stockId) {
		return service.getStock(stockId);
	}

	@Operation(summary = "즉시출고 재고 등록")
	@PostMapping
	public ResponseEntity<InstantResponse> create(@Valid @RequestBody InstantRequest request) {
		return ResponseEntity.status(201).body(service.create(request));
	}

	@Operation(summary = "즉시출고 재고 수정")
	@PutMapping("/{stockId}")
	public InstantResponse update(@PathVariable Long stockId, @Valid @RequestBody InstantRequest request) {
		return service.update(stockId, request);
	}

	@Operation(summary = "즉시출고 재고 삭제")
	@DeleteMapping("/{stockId}")
	public ResponseEntity<Void> delete(@PathVariable Long stockId) {
		service.delete(stockId);
		return ResponseEntity.noContent().build();
	}

}
