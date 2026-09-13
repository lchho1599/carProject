package com.rentdb.lead.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.rentdb.lead.domain.CreditScore;
import com.rentdb.lead.domain.UseType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 이용조건 — 모든 항목 선택 사항. lead.conditions(jsonb)에 값이 있는 항목만 저장한다.
 */
public record LeadConditions(
		UseType useType,

		@Schema(description = "이용기간(개월): 24/36/48/60")
		@Min(value = 12, message = "이용기간은 12개월 이상이어야 합니다.")
		@Max(value = 60, message = "이용기간은 60개월 이하여야 합니다.")
		Integer periodMonths,

		@Schema(description = "보증금 비율(%)")
		@Min(0) @Max(100)
		Integer depositRate,

		@Schema(description = "선납금 비율(%)")
		@Min(0) @Max(100)
		Integer prepayRate,

		@Schema(description = "보험연령: 21 또는 26 (만 N세 이상)")
		@Min(value = 21, message = "보험연령은 21 또는 26입니다.")
		@Max(value = 26, message = "보험연령은 21 또는 26입니다.")
		Integer insuranceAge,

		@Schema(description = "연간 주행거리(km): 10000/20000/30000, 0 = 무제한")
		@Min(0) @Max(100000)
		Integer annualMileage,

		CreditScore creditScore) {

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		putIfPresent(map, "useType", useType == null ? null : useType.name());
		putIfPresent(map, "periodMonths", periodMonths);
		putIfPresent(map, "depositRate", depositRate);
		putIfPresent(map, "prepayRate", prepayRate);
		putIfPresent(map, "insuranceAge", insuranceAge);
		putIfPresent(map, "annualMileage", annualMileage);
		putIfPresent(map, "creditScore", creditScore == null ? null : creditScore.name());
		return map;
	}

	private static void putIfPresent(Map<String, Object> map, String key, Object value) {
		if (value != null) {
			map.put(key, value);
		}
	}

}
