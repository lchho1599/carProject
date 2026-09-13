package com.rentdb.lead.domain;

import java.time.OffsetDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.rentdb.deal.domain.Deal;
import com.rentdb.global.common.BaseTimeEntity;
import com.rentdb.instant.domain.InstantStock;
import com.rentdb.vehicle.domain.Brand;
import com.rentdb.vehicle.domain.VehicleModel;
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

/** 상담 신청(리드) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lead")
public class Lead extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LeadType type;

	@Column(nullable = false, length = 50)
	private String name;

	/** 숫자만 저장 (01012345678) */
	@Column(nullable = false, length = 20)
	private String phone;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_id")
	private Brand brand;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "model_id")
	private VehicleModel model;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trim_id")
	private VehicleTrim trim;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deal_id")
	private Deal deal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instant_stock_id")
	private InstantStock instantStock;

	/** 신청 시점 차량·가격 복사본 — 상품이 바뀌어도 신청 내용 보존 */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "vehicle_snapshot")
	private Map<String, Object> vehicleSnapshot;

	/** 이용조건: 이용방법·기간·보증금·선납·보험연령·주행거리·신용도 */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "conditions")
	private Map<String, Object> conditions;

	@Column(name = "total_price")
	private Long totalPrice;

	@Column(name = "agree_privacy", nullable = false)
	private boolean agreePrivacy;

	@Column(name = "agree_marketing", nullable = false)
	private boolean agreeMarketing;

	@Column(name = "agreed_at", nullable = false)
	private OffsetDateTime agreedAt;

	@Column(name = "source_url", length = 1000)
	private String sourceUrl;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "utm")
	private Map<String, String> utm;

	@Column(name = "ip_hash", length = 64)
	private String ipHash;

	@Column(name = "user_agent", length = 500)
	private String userAgent;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LeadStatus status = LeadStatus.NEW;

	@Builder
	private Lead(LeadType type, String name, String phone, Brand brand, VehicleModel model, VehicleTrim trim,
			Deal deal, InstantStock instantStock, Map<String, Object> vehicleSnapshot,
			Map<String, Object> conditions, Long totalPrice, boolean agreePrivacy, boolean agreeMarketing,
			OffsetDateTime agreedAt, String sourceUrl, Map<String, String> utm, String ipHash, String userAgent) {
		this.type = type;
		this.name = name;
		this.phone = phone;
		this.brand = brand;
		this.model = model;
		this.trim = trim;
		this.deal = deal;
		this.instantStock = instantStock;
		this.vehicleSnapshot = vehicleSnapshot;
		this.conditions = conditions;
		this.totalPrice = totalPrice;
		this.agreePrivacy = agreePrivacy;
		this.agreeMarketing = agreeMarketing;
		this.agreedAt = agreedAt != null ? agreedAt : OffsetDateTime.now();
		this.sourceUrl = sourceUrl;
		this.utm = utm;
		this.ipHash = ipHash;
		this.userAgent = userAgent;
	}

	public void changeStatus(LeadStatus status) {
		this.status = status;
	}

}
