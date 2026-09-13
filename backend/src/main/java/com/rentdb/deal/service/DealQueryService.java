package com.rentdb.deal.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rentdb.deal.domain.DealType;
import com.rentdb.deal.dto.DealResponse;
import com.rentdb.deal.repository.DealRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealQueryService {

	private final DealRepository dealRepository;
	private final Clock clock;

	public List<DealResponse> getVisibleDeals(DealType type) {
		return dealRepository.findVisible(type, OffsetDateTime.now(clock)).stream()
				.map(DealResponse::from)
				.toList();
	}

}
