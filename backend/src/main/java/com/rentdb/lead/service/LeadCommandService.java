package com.rentdb.lead.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.deal.domain.Deal;
import com.rentdb.deal.repository.DealRepository;
import com.rentdb.global.common.Masking;
import com.rentdb.global.error.BusinessException;
import com.rentdb.global.error.ErrorCode;
import com.rentdb.global.web.ClientInfo;
import com.rentdb.instant.domain.InstantStock;
import com.rentdb.instant.repository.InstantStockRepository;
import com.rentdb.lead.LeadProperties;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.dto.LeadCreateRequest;
import com.rentdb.lead.dto.LeadCreateResponse;
import com.rentdb.lead.event.LeadCreatedEvent;
import com.rentdb.lead.repository.LeadRepository;
import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.ModelColor;
import com.rentdb.vehicle.domain.ModelOption;
import com.rentdb.vehicle.domain.VehicleModel;
import com.rentdb.vehicle.domain.VehicleTrim;
import com.rentdb.vehicle.repository.BrandRepository;
import com.rentdb.vehicle.repository.ModelColorRepository;
import com.rentdb.vehicle.repository.ModelOptionRepository;
import com.rentdb.vehicle.repository.VehicleModelRepository;
import com.rentdb.vehicle.repository.VehicleTrimRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상담 신청 처리 (설계 문서 §9.4).
 * (보안 필터: IP 요청 제한) → 검증 → 허니팟 → 중복 확인 → 서버에서 차량·가격 재조회해 스냅샷 생성 → 저장 → 커밋 후 알림 이벤트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadCommandService {

	private final LeadRepository leadRepository;
	private final BrandRepository brandRepository;
	private final VehicleModelRepository modelRepository;
	private final VehicleTrimRepository trimRepository;
	private final ModelOptionRepository optionRepository;
	private final ModelColorRepository colorRepository;
	private final DealRepository dealRepository;
	private final InstantStockRepository instantStockRepository;
	private final LeadProperties leadProperties;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;

	@Transactional
	public LeadCreateResponse create(LeadCreateRequest request, ClientInfo client) {
		if (request.website() != null && !request.website().isBlank()) {
			// 봇이 눈치채지 못하도록 성공처럼 응답하고 저장하지 않는다
			log.info("허니팟 필드가 채워진 신청을 무시했습니다.");
			return new LeadCreateResponse(null);
		}

		// IP당 요청 제한은 보안 필터(RateLimitFilter)가 컨트롤러 앞에서 처리한다
		OffsetDateTime now = OffsetDateTime.now(clock);
		String phone = Masking.digitsOnly(request.phone());

		leadRepository.lockByPhone(phone);
		if (leadRepository.existsByPhoneAndCreatedAtAfter(phone,
				now.minusMinutes(leadProperties.duplicateWindowMinutes()))) {
			throw new BusinessException(ErrorCode.LEAD_DUPLICATED);
		}

		Target target = resolveTarget(request, now);

		Lead lead = Lead.builder()
				.type(request.type())
				.name(request.name().strip())
				.phone(phone)
				.brand(target.brand)
				.model(target.model)
				.trim(target.trim)
				.deal(target.deal)
				.instantStock(target.instantStock)
				.vehicleSnapshot(target.snapshot.isEmpty() ? null : target.snapshot)
				.conditions(request.conditions() == null ? null : request.conditions().toMap())
				.totalPrice(target.totalPrice)
				.agreePrivacy(Boolean.TRUE.equals(request.agreePrivacy()))
				.agreeMarketing(Boolean.TRUE.equals(request.agreeMarketing()))
				.agreedAt(now)
				.sourceUrl(request.sourceUrl())
				.utm(filterUtm(request.utm()))
				.ipHash(client.ipHash())
				.userAgent(client.userAgent())
				.build();
		leadRepository.save(lead);

		eventPublisher.publishEvent(new LeadCreatedEvent(lead.getId()));
		return new LeadCreateResponse(lead.getId());
	}

	// ------------------------------------------------------------------ 유형별 대상 확인·스냅샷

	private Target resolveTarget(LeadCreateRequest request, OffsetDateTime now) {
		return switch (request.type()) {
			case QUICK -> quickTarget(request);
			case ESTIMATE -> estimateTarget(request);
			case DEAL -> dealTarget(request, now);
			case INSTANT -> instantTarget(request);
		};
	}

	/** 빠른상담: 차량 선택은 선택 사항 — 모델을 고르면 브랜드도 함께 저장 */
	private Target quickTarget(LeadCreateRequest request) {
		Target target = new Target();
		if (request.modelId() != null) {
			VehicleModel model = modelRepository.findWithBrandByIdAndActiveTrue(request.modelId())
					.filter(m -> m.getBrand().isActive())
					.orElseThrow(() -> invalid("선택한 차량 모델을 찾을 수 없습니다."));
			target.putModel(model);
		}
		else if (request.brandId() != null) {
			Brand brand = brandRepository.findByIdAndActiveTrue(request.brandId())
					.orElseThrow(() -> invalid("선택한 브랜드를 찾을 수 없습니다."));
			target.brand = brand;
			target.snapshot.put("brand", brand.getName());
		}
		return target;
	}

	/** 간편견적: 트림 필수, 색상·옵션은 같은 모델 소속만 허용, 합계는 서버에서 계산 */
	private Target estimateTarget(LeadCreateRequest request) {
		if (request.trimId() == null) {
			throw invalid("간편견적은 세부모델(트림)을 선택해야 합니다.");
		}
		VehicleTrim trim = findAvailableTrim(request.trimId());
		Long modelId = trim.getModel().getId();

		Target target = new Target();
		target.putTrim(trim);
		long total = trim.getPrice();

		if (request.colorId() != null) {
			ModelColor color = colorRepository.findByIdAndModelIdAndActiveTrue(request.colorId(), modelId)
					.orElseThrow(() -> invalid("선택한 색상을 사용할 수 없습니다."));
			target.snapshot.put("color", Map.of("id", color.getId(), "name", color.getName(),
					"extraPrice", color.getExtraPrice()));
			total += color.getExtraPrice();
		}

		if (request.optionIds() != null && !request.optionIds().isEmpty()) {
			Set<Long> optionIds = new LinkedHashSet<>(request.optionIds());
			List<ModelOption> options = optionRepository
					.findByIdInAndModelIdAndActiveTrueOrderBySortOrderAscIdAsc(optionIds, modelId);
			if (options.size() != optionIds.size()) {
				throw invalid("선택한 옵션 중 사용할 수 없는 항목이 있습니다.");
			}
			List<Map<String, Object>> optionSnapshot = new ArrayList<>();
			for (ModelOption option : options) {
				optionSnapshot.add(Map.of("id", option.getId(), "name", option.getName(), "price", option.getPrice()));
				total += option.getPrice();
			}
			target.snapshot.put("options", optionSnapshot);
		}

		target.totalPrice = total;
		return target;
	}

	/** 특가: 지금 노출 중인 특가만 신청 가능 */
	private Target dealTarget(LeadCreateRequest request, OffsetDateTime now) {
		if (request.dealId() == null) {
			throw invalid("특가 상품 정보가 없습니다.");
		}
		Deal deal = dealRepository.findWithVehicleById(request.dealId())
				.filter(d -> d.isVisibleAt(now))
				.orElseThrow(() -> invalid("신청할 수 없는 특가 상품입니다. 종료되었거나 비공개 상태입니다."));

		Target target = new Target();
		target.deal = deal;
		target.putTrim(deal.getTrim());
		Map<String, Object> dealSnapshot = new LinkedHashMap<>();
		dealSnapshot.put("id", deal.getId());
		dealSnapshot.put("type", deal.getType().name());
		dealSnapshot.put("title", deal.getTitle());
		dealSnapshot.put("monthlyPrice", deal.getMonthlyPrice());
		dealSnapshot.put("periodMonths", deal.getPeriodMonths());
		dealSnapshot.put("depositRate", deal.getDepositRate());
		dealSnapshot.put("prepayRate", deal.getPrepayRate());
		target.snapshot.put("deal", dealSnapshot);
		target.totalPrice = deal.getTrim().getPrice();
		return target;
	}

	/** 즉시출고: 판매완료·비공개 차량은 신청 불가 */
	private Target instantTarget(LeadCreateRequest request) {
		if (request.instantStockId() == null) {
			throw invalid("즉시출고 차량 정보가 없습니다.");
		}
		InstantStock stock = instantStockRepository.findWithVehicleById(request.instantStockId())
				.filter(InstantStock::isVisible)
				.orElseThrow(() -> invalid("신청할 수 없는 즉시출고 차량입니다. 판매완료되었거나 비공개 상태입니다."));

		Target target = new Target();
		target.instantStock = stock;
		target.putTrim(stock.getTrim());
		Map<String, Object> stockSnapshot = new LinkedHashMap<>();
		stockSnapshot.put("id", stock.getId());
		stockSnapshot.put("exteriorColor", stock.getExteriorColor());
		stockSnapshot.put("interiorColor", stock.getInteriorColor());
		stockSnapshot.put("optionsText", stock.getOptionsText());
		stockSnapshot.put("vehiclePrice", stock.getVehiclePrice());
		stockSnapshot.put("monthlyPrice", stock.getMonthlyPrice());
		stockSnapshot.put("conditionText", stock.getConditionText());
		stockSnapshot.values().removeIf(java.util.Objects::isNull);
		target.snapshot.put("instantStock", stockSnapshot);
		target.totalPrice = stock.getVehiclePrice();
		return target;
	}

	private VehicleTrim findAvailableTrim(Long trimId) {
		return trimRepository.findWithModelById(trimId)
				.filter(VehicleTrim::isAvailable)
				.orElseThrow(() -> invalid("선택한 세부모델(트림)을 찾을 수 없습니다."));
	}

	private Map<String, String> filterUtm(Map<String, String> utm) {
		if (utm == null) {
			return null;
		}
		Map<String, String> filtered = new LinkedHashMap<>();
		utm.forEach((key, value) -> {
			if (key != null && key.startsWith("utm_") && value != null && !value.isBlank()) {
				filtered.put(key, value);
			}
		});
		return filtered.isEmpty() ? null : filtered;
	}

	private static BusinessException invalid(String message) {
		return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
	}

	/** 유형별로 확인한 신청 대상과 스냅샷 */
	private static final class Target {

		private Brand brand;
		private VehicleModel model;
		private VehicleTrim trim;
		private Deal deal;
		private InstantStock instantStock;
		private Long totalPrice;
		private final Map<String, Object> snapshot = new LinkedHashMap<>();

		void putModel(VehicleModel model) {
			this.model = model;
			this.brand = model.getBrand();
			snapshot.put("brand", brand.getName());
			snapshot.put("model", model.getName());
		}

		void putTrim(VehicleTrim trim) {
			putModel(trim.getModel());
			this.trim = trim;
			snapshot.put("trim", trim.getName());
			snapshot.put("trimPrice", trim.getPrice());
		}

	}

}
