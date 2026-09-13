package com.rentdb.lead.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rentdb.lead.domain.LeadStatus;
import com.rentdb.lead.domain.LeadType;

/**
 * 리드 표시용 한글 라벨 — 알림 메일, CSV 다운로드에서 함께 사용한다.
 */
public final class LeadLabels {

	private LeadLabels() {
	}

	public static String type(LeadType type) {
		return switch (type) {
			case QUICK -> "빠른상담";
			case ESTIMATE -> "간편견적";
			case DEAL -> "특가";
			case INSTANT -> "즉시출고";
		};
	}

	public static String status(LeadStatus status) {
		return switch (status) {
			case NEW -> "신규";
			case IN_PROGRESS -> "상담중";
			case CONTRACTED -> "계약완료";
			case NO_ANSWER -> "부재";
			case CANCELED -> "취소";
		};
	}

	/** 스냅샷의 "브랜드 모델", 없으면 "차량 미선택" */
	public static String vehicleName(Map<String, Object> snapshot) {
		if (snapshot == null) {
			return "차량 미선택";
		}
		List<String> parts = new ArrayList<>();
		if (snapshot.get("brand") != null) {
			parts.add(String.valueOf(snapshot.get("brand")));
		}
		if (snapshot.get("model") != null) {
			parts.add(String.valueOf(snapshot.get("model")));
		}
		return parts.isEmpty() ? "차량 미선택" : String.join(" ", parts);
	}

	/** 차량 상세: "트림 / 색상: 화이트 / 옵션: 선루프, 빌트인 캠 / 특가: 제목 (월 209,000원)" */
	public static String vehicleDetail(Map<String, Object> snapshot) {
		if (snapshot == null) {
			return "";
		}
		List<String> parts = new ArrayList<>();
		if (snapshot.get("trim") != null) {
			parts.add(String.valueOf(snapshot.get("trim")));
		}
		if (snapshot.get("color") instanceof Map<?, ?> color) {
			parts.add("색상: " + color.get("name"));
		}
		if (snapshot.get("options") instanceof List<?> options && !options.isEmpty()) {
			parts.add("옵션: " + String.join(", ", options.stream()
					.map(option -> option instanceof Map<?, ?> map ? String.valueOf(map.get("name")) : String.valueOf(option))
					.toList()));
		}
		if (snapshot.get("deal") instanceof Map<?, ?> deal) {
			parts.add("특가: " + deal.get("title") + " (월 %,d원)".formatted(toLong(deal.get("monthlyPrice"))));
		}
		if (snapshot.get("instantStock") instanceof Map<?, ?> stock) {
			parts.add("즉시출고: " + stock.get("exteriorColor") + " (월 %,d원)".formatted(toLong(stock.get("monthlyPrice"))));
		}
		return String.join(" / ", parts);
	}

	private static long toLong(Object value) {
		return value instanceof Number number ? number.longValue() : 0L;
	}

	/** 이용조건 요약: "리스 · 36개월 · 선납금 30% · 주행거리 무제한" */
	public static String conditionSummary(Map<String, Object> conditions) {
		if (conditions == null || conditions.isEmpty()) {
			return "";
		}
		List<String> parts = new ArrayList<>();
		Object useType = conditions.get("useType");
		if (useType != null) {
			parts.add("RENT".equals(useType) ? "장기렌트" : "LEASE".equals(useType) ? "리스" : String.valueOf(useType));
		}
		if (conditions.get("periodMonths") != null) {
			parts.add(conditions.get("periodMonths") + "개월");
		}
		if (conditions.get("depositRate") != null) {
			parts.add("보증금 " + conditions.get("depositRate") + "%");
		}
		if (conditions.get("prepayRate") != null) {
			parts.add("선납금 " + conditions.get("prepayRate") + "%");
		}
		if (conditions.get("insuranceAge") != null) {
			parts.add("만 " + conditions.get("insuranceAge") + "세 이상");
		}
		if (conditions.get("annualMileage") != null) {
			long km = conditions.get("annualMileage") instanceof Number number ? number.longValue() : 0L;
			parts.add(km == 0 ? "주행거리 무제한" : "연 " + (km / 10000) + "만km");
		}
		Object credit = conditions.get("creditScore");
		if (credit != null) {
			parts.add(switch (String.valueOf(credit)) {
				case "UNDER_700" -> "신용 700점 미만";
				case "OVER_700" -> "신용 700점 이상";
				default -> "신용도 모름";
			});
		}
		return String.join(" · ", parts);
	}

	/** 01012345678 → 010-1234-5678 */
	public static String formatPhone(String digits) {
		if (digits == null) {
			return "";
		}
		if (digits.length() == 11) {
			return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
		}
		if (digits.length() == 10) {
			return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
		}
		return digits;
	}

}
