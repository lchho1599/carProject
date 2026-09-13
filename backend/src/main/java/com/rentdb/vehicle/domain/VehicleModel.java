package com.rentdb.vehicle.domain;

import com.rentdb.global.common.BaseTimeEntity;

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

/** 차량 모델 (예: 기아 쏘렌토) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vehicle_model")
public class VehicleModel extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "brand_id", nullable = false)
	private Brand brand;

	@Column(nullable = false, length = 100)
	private String name;

	/** 차급 (경차, 준중형, 중형 ...) */
	@Column(length = 30)
	private String segment;

	@Enumerated(EnumType.STRING)
	@Column(name = "body_type", nullable = false, length = 20)
	private BodyType bodyType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FuelType fuel;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Builder
	private VehicleModel(Brand brand, String name, String segment, BodyType bodyType, FuelType fuel,
			String imageUrl, int sortOrder) {
		this.brand = brand;
		this.name = name;
		this.segment = segment;
		this.bodyType = bodyType;
		this.fuel = fuel;
		this.imageUrl = imageUrl;
		this.sortOrder = sortOrder;
	}

	public void update(String name, String segment, BodyType bodyType, FuelType fuel, String imageUrl, int sortOrder,
			boolean active) {
		this.name = name;
		this.segment = segment;
		this.bodyType = bodyType;
		this.fuel = fuel;
		this.imageUrl = imageUrl;
		this.sortOrder = sortOrder;
		this.active = active;
	}

}
