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

/** 모델별 외장 색상 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "model_color")
public class ModelColor extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "model_id", nullable = false)
	private VehicleModel model;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "hex_code", length = 7)
	private String hexCode;

	@Column(name = "extra_price", nullable = false)
	private long extraPrice;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Builder
	private ModelColor(VehicleModel model, String name, String hexCode, long extraPrice, int sortOrder) {
		this.model = model;
		this.name = name;
		this.hexCode = hexCode;
		this.extraPrice = extraPrice;
		this.sortOrder = sortOrder;
	}

	public void update(String name, String hexCode, long extraPrice, int sortOrder, boolean active) {
		this.name = name;
		this.hexCode = hexCode;
		this.extraPrice = extraPrice;
		this.sortOrder = sortOrder;
		this.active = active;
	}

}
