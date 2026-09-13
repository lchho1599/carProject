package com.rentdb.banner.domain;

import java.time.OffsetDateTime;

import com.rentdb.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메인 배너 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "banner")
public class Banner extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "image_pc_url", nullable = false, length = 500)
	private String imagePcUrl;

	@Column(name = "image_mobile_url", nullable = false, length = 500)
	private String imageMobileUrl;

	@Column(name = "link_url", length = 500)
	private String linkUrl;

	@Column(name = "starts_at", nullable = false)
	private OffsetDateTime startsAt;

	@Column(name = "ends_at")
	private OffsetDateTime endsAt;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "is_published", nullable = false)
	private boolean published;

	@Builder
	private Banner(String title, String imagePcUrl, String imageMobileUrl, String linkUrl,
			OffsetDateTime startsAt, OffsetDateTime endsAt, int sortOrder, boolean published) {
		this.title = title;
		this.imagePcUrl = imagePcUrl;
		this.imageMobileUrl = imageMobileUrl;
		this.linkUrl = linkUrl;
		this.startsAt = startsAt != null ? startsAt : OffsetDateTime.now();
		this.endsAt = endsAt;
		this.sortOrder = sortOrder;
		this.published = published;
	}

	public void update(String title, String imagePcUrl, String imageMobileUrl, String linkUrl,
			OffsetDateTime startsAt, OffsetDateTime endsAt, int sortOrder, boolean published) {
		this.title = title;
		this.imagePcUrl = imagePcUrl;
		this.imageMobileUrl = imageMobileUrl;
		this.linkUrl = linkUrl;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
		this.sortOrder = sortOrder;
		this.published = published;
	}

}
