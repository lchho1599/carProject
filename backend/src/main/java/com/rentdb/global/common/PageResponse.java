package com.rentdb.global.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/** 목록 응답 형식: { content, page, size, totalElements, totalPages } (page 는 0부터) */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
		return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
				page.getTotalElements(), page.getTotalPages());
	}

}
