package com.rentdb.lead.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rentdb.lead.domain.Lead;

/** QueryDSL 로 구현하는 리드 검색 (LeadRepositoryCustomImpl) */
public interface LeadRepositoryCustom {

	/** 관리자 목록 — 최신순, 페이지 */
	Page<Lead> search(LeadSearch search, Pageable pageable);

	/** CSV 다운로드 — 최신순, 최대 limit 건 */
	List<Lead> searchForExport(LeadSearch search, int limit);

}
