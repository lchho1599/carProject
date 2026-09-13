package com.rentdb.global.web;

/**
 * 요청자 정보 — IP는 원문을 저장하지 않고 해시만 사용한다.
 */
public record ClientInfo(String ipHash, String userAgent) {
}
