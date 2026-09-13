package com.rentdb.global.common;

/**
 * 개인정보 마스킹 — 관리자 목록·알림 메일에서 사용한다.
 */
public final class Masking {

	private Masking() {
	}

	/**
	 * 이름: 첫 글자와 마지막 글자만 남긴다.
	 * 홍길동 → 홍*동, 홍길 → 홍*, 남궁민수 → 남**수, 홍 → *
	 */
	public static String name(String name) {
		if (name == null || name.isBlank()) {
			return "";
		}
		String trimmed = name.strip();
		int length = trimmed.codePointCount(0, trimmed.length());
		if (length == 1) {
			return "*";
		}
		int[] cps = trimmed.codePoints().toArray();
		if (length == 2) {
			return new String(cps, 0, 1) + "*";
		}
		return new String(cps, 0, 1) + "*".repeat(length - 2) + new String(cps, length - 1, 1);
	}

	/**
	 * 휴대폰: 가운데 자리를 가린다. 01012345678 → 010-****-5678, 0111234567 → 011-***-4567
	 */
	public static String phone(String phone) {
		String digits = digitsOnly(phone);
		if (digits.length() < 10) {
			return "*".repeat(digits.length());
		}
		String head = digits.substring(0, 3);
		String tail = digits.substring(digits.length() - 4);
		int middleLength = digits.length() - 7;
		return head + "-" + "*".repeat(middleLength) + "-" + tail;
	}

	public static String digitsOnly(String value) {
		return value == null ? "" : value.replaceAll("\\D", "");
	}

}
