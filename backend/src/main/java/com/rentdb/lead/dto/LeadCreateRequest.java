package com.rentdb.lead.dto;

import java.util.List;
import java.util.Map;

import com.rentdb.lead.domain.LeadType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 상담 신청 요청.
 * 유형별 필수 참조: ESTIMATE=trimId, DEAL=dealId, INSTANT=instantStockId, QUICK=없음(모델·브랜드 선택 시 함께 저장).
 * 가격은 받지 않는다 — 서버가 참조 ID로 다시 조회해 계산한다.
 */
public record LeadCreateRequest(
		@NotNull(message = "신청 유형은 필수입니다.")
		LeadType type,

		@NotBlank(message = "이름을 입력해 주세요.")
		@Size(min = 2, max = 20, message = "이름은 2~20자로 입력해 주세요.")
		String name,

		@NotBlank(message = "연락처를 입력해 주세요.")
		@Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
		String phone,

		// Jackson 3은 JSON에 없는 primitive(boolean)를 오류로 처리하므로 Boolean으로 받는다
		@NotNull(message = "개인정보 수집·이용에 동의해 주세요.")
		@AssertTrue(message = "개인정보 수집·이용에 동의해 주세요.")
		Boolean agreePrivacy,

		@Schema(description = "마케팅 수신 동의 (생략하면 미동의)")
		Boolean agreeMarketing,

		@Positive Long brandId,
		@Positive Long modelId,
		@Positive Long trimId,

		@Schema(description = "간편견적: 외장색상 ID")
		@Positive Long colorId,

		@Schema(description = "간편견적: 선택 옵션 ID 목록")
		@Size(max = 30) List<@NotNull @Positive Long> optionIds,

		@Positive Long dealId,
		@Positive Long instantStockId,

		@Valid LeadConditions conditions,

		@Schema(description = "신청한 페이지 경로")
		@Size(max = 1000) String sourceUrl,

		@Schema(description = "utm_source 등 광고 파라미터 (utm_ 로 시작하는 키만 저장)")
		@Size(max = 10) Map<@Size(max = 50) String, @Size(max = 200) String> utm,

		@Schema(description = "스팸 방지용 숨김 필드 — 화면에서는 항상 비워서 보낸다", hidden = true)
		String website) {
}
