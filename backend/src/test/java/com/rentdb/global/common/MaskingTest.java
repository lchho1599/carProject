package com.rentdb.global.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MaskingTest {

	@Test
	void 이름은_첫글자와_마지막글자만_남긴다() {
		assertThat(Masking.name("홍길동")).isEqualTo("홍*동");
		assertThat(Masking.name("홍길")).isEqualTo("홍*");
		assertThat(Masking.name("남궁민수")).isEqualTo("남**수");
		assertThat(Masking.name("홍")).isEqualTo("*");
		assertThat(Masking.name("  김철수 ")).isEqualTo("김*수");
		assertThat(Masking.name(null)).isEmpty();
	}

	@Test
	void 연락처는_가운데를_가린다() {
		assertThat(Masking.phone("01012345678")).isEqualTo("010-****-5678");
		assertThat(Masking.phone("010-1234-5678")).isEqualTo("010-****-5678");
		assertThat(Masking.phone("0111234567")).isEqualTo("011-***-4567");
		assertThat(Masking.phone("123")).isEqualTo("***");
	}

}
