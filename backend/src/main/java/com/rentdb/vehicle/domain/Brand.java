package com.rentdb.vehicle.domain;

import com.rentdb.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "brand")
public class Brand extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Origin origin;

	@Column(name = "logo_url", length = 500)
	private String logoUrl;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Builder
	private Brand(String name, Origin origin, String logoUrl, int sortOrder) {
		this.name = name;
		this.origin = origin;
		this.logoUrl = logoUrl;
		this.sortOrder = sortOrder;
	}

	public void update(String name, Origin origin, String logoUrl, int sortOrder, boolean active) {
		this.name = name;
		this.origin = origin;
		this.logoUrl = logoUrl;
		this.sortOrder = sortOrder;
		this.active = active;
	}

}
