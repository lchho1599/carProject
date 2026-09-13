package com.rentdb.global.error;

import lombok.Getter;

/**
 * 서비스에서 의도적으로 발생시키는 예외 — GlobalExceptionHandler가 ErrorResponse로 변환한다.
 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, errorCode.getMessage());
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public static BusinessException notFound(String target) {
		return new BusinessException(ErrorCode.NOT_FOUND, target + "을(를) 찾을 수 없습니다.");
	}

}
