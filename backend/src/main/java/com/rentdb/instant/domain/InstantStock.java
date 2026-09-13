package com.rentdb.instant.domain;

import com.rentdb.global.common.BaseTimeEntity;
import com.rentdb.vehicle.domain.VehicleTrim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 즉시출고 재고 차량 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "instant_stock")
public class InstantStock extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trim_id", nullable = false)
	private VehicleTrim trim;

	@Column(name = "exterior_color", nullable = false, length = 100)
	private String exteriorColor;

	@Column(name = "interior_color", length = 100)
	private String interiorColor;

	@Column(name = "options_text", length = 500)
	private String optionsText;

	@Column(name = "vehicle_price", nullable = false)
	private long vehiclePrice;

	@Column(name = "monthly_price", nullable = false)
	private long monthlyPrice;

	/** 예: 48개월 / 선납금 30% 기준 */
	@Column(name = "condition_text", nullable = false, length = 100)
	private String conditionText;

	@Column(length = 30)
	private String badge;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StockStatus status = StockStatus.AVAILABLE;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "is_published", nullable = false)
	private boolean published;

	/** 상담 신청 가능한지 — 공개 + 판매완료 아님 + 차량 활성 (InstantStockRepository.findVisible 과 같은 규칙) */
	public boolean isVisible() {
		return published && status != StockStatus.SOLD && trim.isAvailable();
	}

	@Builder
	private InstantStock(VehicleTrim trim, String exteriorColor, String interiorColor, String optionsText,
			long vehiclePrice, long monthlyPrice, String conditionText, String badge, StockStatus status,
			int sortOrder, boolean published) {
		this.trim = trim;
		this.exteriorColor = exteriorColor;
		this.interiorColor = interiorColor;
		this.optionsText = optionsText;
		this.vehiclePrice = vehiclePrice;
		this.monthlyPrice = monthlyPrice;
		this.conditionText = conditionText;
		this.badge = badge;
		this.status = status != null ? status : StockStatus.AVAILABLE;
		this.sortOrder = sortOrder;
		this.published = published;
	}

	public void update(VehicleTrim trim, String exteriorColor, String interiorColor, String optionsText,
			long vehiclePrice, long monthlyPrice, String conditionText, String badge, StockStatus status,
			int sortOrder, boolean published) {
		this.trim = trim;
		this.exteriorColor = exteriorColor;
		this.interiorColor = interiorColor;
		this.optionsText = optionsText;
		this.vehiclePrice = vehiclePrice;
		this.monthlyPrice = monthlyPrice;
		this.conditionText = conditionText;
		this.badge = badge;
		this.status = status;
		this.sortOrder = sortOrder;
		this.published = published;
	}

}
