package com.rentdb.vehicle.domain;

/** 연료 — 즉시출고 "전기차" 필터는 EV로 거른다. */
public enum FuelType {
	GASOLINE,
	DIESEL,
	HYBRID,
	EV,
	LPG
}
