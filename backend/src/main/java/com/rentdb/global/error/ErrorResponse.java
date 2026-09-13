package com.rentdb.global.error;

import java.util.List;

/**
 * 오류 응답 형식: { code, message, fieldErrors: [{ field, message }] }
 */
public record ErrorResponse(String code, String message, List<FieldError> fieldErrors) {

	public record FieldError(String field, String message) {
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.name(), message, List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors);
	}

}
