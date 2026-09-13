package com.rentdb.instant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;
import com.rentdb.instant.domain.InstantStock;
import com.rentdb.instant.domain.StockStatus;
import com.rentdb.instant.dto.admin.AdminInstantDtos.InstantRequest;
import com.rentdb.instant.dto.admin.AdminInstantDtos.InstantResponse;
import com.rentdb.instant.repository.InstantStockRepository;
import com.rentdb.vehicle.domain.VehicleTrim;
import com.rentdb.vehicle.repository.VehicleTrimRepository;

import lombok.RequiredArgsConstructor;

/** 관리자 즉시출고 재고 관리 — 판매가 끝난 차량은 삭제보다 "판매완료" 상태 변경을 권장 (신청 이력 연결 유지) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInstantService {

	private final InstantStockRepository stockRepository;
	private final VehicleTrimRepository trimRepository;

	public List<InstantResponse> getStocks(StockStatus status) {
		List<InstantStock> stocks = status == null
				? stockRepository.findAllByOrderBySortOrderAscIdAsc()
				: stockRepository.findByStatusOrderBySortOrderAscIdAsc(status);
		return stocks.stream().map(InstantResponse::from).toList();
	}

	public InstantResponse getStock(Long stockId) {
		return InstantResponse.from(findStock(stockId));
	}

	@Transactional
	public InstantResponse create(InstantRequest request) {
		VehicleTrim trim = findTrim(request.trimId());
		InstantStock stock = InstantStock.builder()
				.trim(trim)
				.exteriorColor(request.exteriorColor().strip())
				.vehiclePrice(request.vehiclePrice())
				.monthlyPrice(request.monthlyPrice())
				.conditionText(request.conditionText().strip())
				.build();
		apply(stock, trim, request);
		return InstantResponse.from(stockRepository.save(stock));
	}

	@Transactional
	public InstantResponse update(Long stockId, InstantRequest request) {
		InstantStock stock = findStock(stockId);
		apply(stock, findTrim(request.trimId()), request);
		return InstantResponse.from(stock);
	}

	@Transactional
	public void delete(Long stockId) {
		stockRepository.delete(findStock(stockId));
	}

	private void apply(InstantStock stock, VehicleTrim trim, InstantRequest request) {
		stock.update(trim, request.exteriorColor().strip(), blankToNull(request.interiorColor()),
				blankToNull(request.optionsText()), request.vehiclePrice(), request.monthlyPrice(),
				request.conditionText().strip(), blankToNull(request.badge()), request.status(), request.sortOrder(),
				request.published());
	}

	private InstantStock findStock(Long stockId) {
		return stockRepository.findWithVehicleById(stockId).orElseThrow(() -> BusinessException.notFound("즉시출고 차량"));
	}

	private VehicleTrim findTrim(Long trimId) {
		return trimRepository.findWithModelById(trimId)
				.orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "선택한 세부모델을 찾을 수 없습니다."));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
