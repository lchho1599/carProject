package com.rentdb.lead.repository;

import java.time.LocalDate;

import com.rentdb.lead.domain.LeadStatus;
import com.rentdb.lead.domain.LeadType;

/**
 * 관리자 리드 목록 검색 조건 — 값이 없는 조건은 적용하지 않는다.
 *
 * @param from   신청일 시작 (한국 날짜, 포함)
 * @param to     신청일 끝 (한국 날짜, 포함)
 * @param q      숫자만(하이픈·공백 무시) 2~11자리면 연락처 일부, 그 밖에는 이름 일부
 */
public record LeadSearch(LocalDate from, LocalDate to, LeadType type, LeadStatus status, String q) {
}
