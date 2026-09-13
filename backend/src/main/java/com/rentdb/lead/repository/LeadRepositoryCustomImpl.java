package com.rentdb.lead.repository;

import static com.rentdb.lead.domain.QLead.lead;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rentdb.global.config.TimeConfig;
import com.rentdb.lead.domain.Lead;
import com.rentdb.lead.domain.LeadStatus;
import com.rentdb.lead.domain.LeadType;

import lombok.RequiredArgsConstructor;

/**
 * 리드 동적 검색 (QueryDSL).
 * 조건 메서드가 null 을 돌려주면 where 절에서 자동으로 빠진다.
 */
@RequiredArgsConstructor
public class LeadRepositoryCustomImpl implements LeadRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Lead> search(LeadSearch search, Pageable pageable) {
		List<Lead> content = selectLeads(search)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		Long total = queryFactory
				.select(lead.count())
				.from(lead)
				.where(conditions(search))
				.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}

	@Override
	public List<Lead> searchForExport(LeadSearch search, int limit) {
		return selectLeads(search).limit(limit).fetch();
	}

	private JPAQuery<Lead> selectLeads(LeadSearch search) {
		return queryFactory
				.selectFrom(lead)
				.where(conditions(search))
				.orderBy(lead.createdAt.desc(), lead.id.desc());
	}

	private BooleanExpression[] conditions(LeadSearch search) {
		return new BooleanExpression[] {
				createdFrom(search.from()),
				createdTo(search.to()),
				typeEquals(search.type()),
				statusEquals(search.status()),
				keyword(search.q())
		};
	}

	// ---------------------------------------------------------------- 조건 (값이 없으면 null)

	/** 한국 시간 from 0시부터 */
	private BooleanExpression createdFrom(LocalDate from) {
		return from == null ? null : lead.createdAt.goe(from.atStartOfDay(TimeConfig.KST).toOffsetDateTime());
	}

	/** 한국 시간 to 다음날 0시 전까지 */
	private BooleanExpression createdTo(LocalDate to) {
		return to == null ? null : lead.createdAt.lt(to.plusDays(1).atStartOfDay(TimeConfig.KST).toOffsetDateTime());
	}

	private BooleanExpression typeEquals(LeadType type) {
		return type == null ? null : lead.type.eq(type);
	}

	private BooleanExpression statusEquals(LeadStatus status) {
		return status == null ? null : lead.status.eq(status);
	}

	/** 숫자만 입력하면(하이픈·공백 무시) 연락처 일부, 그 밖에는 이름 일부 */
	private BooleanExpression keyword(String q) {
		if (q == null || q.isBlank()) {
			return null;
		}
		String trimmed = q.strip();
		String digits = trimmed.replaceAll("[\\s-]", "");
		if (digits.matches("\\d{2,11}")) {
			return lead.phone.contains(digits);
		}
		// contains 는 검색어의 % _ 를 자동으로 이스케이프한다 (like ? escape '!') — 테스트로 검증
		return lead.name.contains(trimmed);
	}

}
