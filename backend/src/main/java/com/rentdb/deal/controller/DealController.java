package com.rentdb.deal.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentdb.deal.domain.DealType;
import com.rentdb.deal.dto.DealResponse;
import com.rentdb.deal.service.DealQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "특가")
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

	private final DealQueryService dealQueryService;

	@Operation(summary = "노출 중인 특가 목록", description = "type을 비우면 전체 (TIME_SALE=타임특가, NO_DEPOSIT=무보증특가)")
	@GetMapping
	public List<DealResponse> deals(@RequestParam(required = false) DealType type) {
		return dealQueryService.getVisibleDeals(type);
	}

}
