package com.rentdb.deal.domain;

import java.time.OffsetDateTime;

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

/** 특가 상품 (타임특가 / 무보증특가) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "deal")
public class Deal extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DealType type;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trim_id", nullable = false)
	private VehicleTrim trim;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(length = 30)
	private String badge;

	/** 정가 월납입료 (취소선 표시) */
	@Column(name = "original_monthly")
	private Long originalMonthly;

	@Column(name = "monthly_price", nullable = false)
	private long monthlyPrice;

	@Column(name = "lease_monthly")
	private Long leaseMonthly;

	@Column(name = "period_months", nullable = false)
	private int periodMonths;

	@Column(name = "deposit_rate", nullable = false)
	private int depositRate;

	@Column(name = "prepay_rate", nullable = false)
	private int prepayRate;

	@Column(name = "starts_at", nullable = false)
	private OffsetDateTime startsAt;

	@Column(name = "ends_at")
	private OffsetDateTime endsAt;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "is_published", nullable = false)
	private boolean published;

	/** 사용자에게 노출 중인지 — 공개 + 노출 기간 안 + 차량 활성 (DealRepository.findVisible 과 같은 규칙) */
	public boolean isVisibleAt(OffsetDateTime now) {
		return published
				&& !startsAt.isAfter(now)
				&& (endsAt == null || endsAt.isAfter(now))
				&& trim.isAvailable();
	}

	@Builder
	private Deal(DealType type, VehicleTrim trim, String title, String badge, Long originalMonthly,
			long monthlyPrice, Long leaseMonthly, int periodMonths, int depositRate, int prepayRate,
			OffsetDateTime startsAt, OffsetDateTime endsAt, int sortOrder, boolean published) {
		this.type = type;
		this.trim = trim;
		this.title = title;
		this.badge = badge;
		this.originalMonthly = originalMonthly;
		this.monthlyPrice = monthlyPrice;
		this.leaseMonthly = leaseMonthly;
		this.periodMonths = periodMonths;
		this.depositRate = depositRate;
		this.prepayRate = prepayRate;
		this.startsAt = startsAt != null ? startsAt : OffsetDateTime.now();
		this.endsAt = endsAt;
		this.sortOrder = sortOrder;
		this.published = published;
	}

	public void update(DealType type, VehicleTrim trim, String title, String badge, Long originalMonthly,
			long monthlyPrice, Long leaseMonthly, int periodMonths, int depositRate, int prepayRate,
			OffsetDateTime startsAt, OffsetDateTime endsAt, int sortOrder, boolean published) {
		this.type = type;
		this.trim = trim;
		this.title = title;
		this.badge = badge;
		this.originalMonthly = originalMonthly;
		this.monthlyPrice = monthlyPrice;
		this.leaseMonthly = leaseMonthly;
		this.periodMonths = periodMonths;
		this.depositRate = depositRate;
		this.prepayRate = prepayRate;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
		this.sortOrder = sortOrder;
		this.published = published;
	}

}
