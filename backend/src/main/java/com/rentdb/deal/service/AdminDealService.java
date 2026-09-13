package com.rentdb.deal.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.deal.domain.Deal;
import com.rentdb.deal.domain.DealType;
import com.rentdb.deal.dto.admin.AdminDealDtos.DealRequest;
import com.rentdb.deal.dto.admin.AdminDealDtos.DealResponse;
import com.rentdb.deal.repository.DealRepository;
import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;
import com.rentdb.vehicle.domain.VehicleTrim;
import com.rentdb.vehicle.repository.VehicleTrimRepository;

import lombok.RequiredArgsConstructor;

/** 관리자 특가 관리 — 삭제하면 이 특가로 들어온 상담 신청의 특가 연결만 끊기고(신청 내용 스냅샷은 유지) 삭제된다 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDealService {

	private static final Set<Integer> PERIODS = Set.of(24, 36, 48, 60);

	private final DealRepository dealRepository;
	private final VehicleTrimRepository trimRepository;
	private final Clock clock;

	public List<DealResponse> getDeals(DealType type) {
		OffsetDateTime now = OffsetDateTime.now(clock);
		List<Deal> deals = type == null
				? dealRepository.findAllByOrderByTypeAscSortOrderAscIdAsc()
				: dealRepository.findByTypeOrderBySortOrderAscIdAsc(type);
		return deals.stream().map(deal -> DealResponse.of(deal, now)).toList();
	}

	public DealResponse getDeal(Long dealId) {
		return DealResponse.of(findDeal(dealId), OffsetDateTime.now(clock));
	}

	@Transactional
	public DealResponse create(DealRequest request) {
		validate(request);
		VehicleTrim trim = findTrim(request.trimId());
		Deal deal = Deal.builder()
				.type(request.type())
				.trim(trim)
				.title(request.title().strip())
				.monthlyPrice(request.monthlyPrice())
				.periodMonths(request.periodMonths())
				.startsAt(request.startsAt())
				.build();
		apply(deal, trim, request);
		return DealResponse.of(dealRepository.save(deal), OffsetDateTime.now(clock));
	}

	@Transactional
	public DealResponse update(Long dealId, DealRequest request) {
		validate(request);
		Deal deal = findDeal(dealId);
		apply(deal, findTrim(request.trimId()), request);
		return DealResponse.of(deal, OffsetDateTime.now(clock));
	}

	@Transactional
	public void delete(Long dealId) {
		dealRepository.delete(findDeal(dealId));
	}

	private void apply(Deal deal, VehicleTrim trim, DealRequest request) {
		deal.update(request.type(), trim, request.title().strip(), blankToNull(request.badge()),
				request.originalMonthly(), request.monthlyPrice(), request.leaseMonthly(), request.periodMonths(),
				request.depositRate(), request.prepayRate(), request.startsAt(), request.endsAt(), request.sortOrder(),
				request.published());
	}

	private void validate(DealRequest request) {
		if (!PERIODS.contains(request.periodMonths())) {
			throw invalid("이용기간은 24·36·48·60개월 중에서 선택해 주세요.");
		}
		if (request.endsAt() != null && !request.endsAt().isAfter(request.startsAt())) {
			throw invalid("노출 종료 일시는 시작 일시보다 뒤여야 합니다.");
		}
	}

	private Deal findDeal(Long dealId) {
		return dealRepository.findWithVehicleById(dealId).orElseThrow(() -> BusinessException.notFound("특가"));
	}

	private VehicleTrim findTrim(Long trimId) {
		return trimRepository.findWithModelById(trimId).orElseThrow(() -> invalid("선택한 세부모델을 찾을 수 없습니다."));
	}

	private static BusinessException invalid(String message) {
		return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
