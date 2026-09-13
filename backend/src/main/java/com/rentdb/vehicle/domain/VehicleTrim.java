package com.rentdb.vehicle.domain;

import com.rentdb.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** 트림 (연식·엔진·세부모델, 차량 기본가) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vehicle_trim")
public class VehicleTrim extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "model_id", nullable = false)
	private VehicleModel model;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(nullable = false)
	private long price;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	/** 트림·모델·브랜드가 모두 활성이어야 사용자 화면·신청에 쓸 수 있다 (model, brand 로딩 필요) */
	public boolean isAvailable() {
		return active && model.isActive() && model.getBrand().isActive();
	}

	@Builder
	private VehicleTrim(VehicleModel model, String name, long price, int sortOrder) {
		this.model = model;
		this.name = name;
		this.price = price;
		this.sortOrder = sortOrder;
	}

	public void update(String name, long price, int sortOrder, boolean active) {
		this.name = name;
		this.price = price;
		this.sortOrder = sortOrder;
		this.active = active;
	}

}
