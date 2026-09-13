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

/** 모델별 선택 옵션 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "model_option")
public class ModelOption extends BaseTimeEntity {

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

	@Builder
	private ModelOption(VehicleModel model, String name, long price, int sortOrder) {
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
