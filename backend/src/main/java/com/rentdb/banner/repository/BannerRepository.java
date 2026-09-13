package com.rentdb.banner.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentdb.banner.domain.Banner;

public interface BannerRepository extends JpaRepository<Banner, Long> {

	@Query("""
			select b from Banner b
			where b.published = true
			  and b.startsAt <= :now
			  and (b.endsAt is null or b.endsAt > :now)
			order by b.sortOrder, b.id
			""")
	List<Banner> findVisible(@Param("now") OffsetDateTime now);

	// 관리자: 비공개·종료 포함
	List<Banner> findAllByOrderBySortOrderAscIdAsc();

}
