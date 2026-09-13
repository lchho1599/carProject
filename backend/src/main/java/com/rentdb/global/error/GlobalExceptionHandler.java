package com.rentdb.global.error;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, e.getMessage()));
	}

	/** @RequestBody 검증 실패 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException e) {
		List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
				.toList();
		return badRequest(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, fieldErrors));
	}

	/** @RequestParam / @PathVariable 제약 검증 실패 */
	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorResponse> handleParamValidation(HandlerMethodValidationException e) {
		List<ErrorResponse.FieldError> fieldErrors = e.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream()
						.map(err -> new ErrorResponse.FieldError(
								result.getMethodParameter().getParameterName(), err.getDefaultMessage())))
				.toList();
		return badRequest(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, fieldErrors));
	}

	/** 파라미터 타입 불일치 (예: type=ABC, id=abc) */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return badRequest(ErrorResponse.of(ErrorCode.VALIDATION_FAILED,
				List.of(new ErrorResponse.FieldError(e.getName(), "허용되지 않는 값입니다: " + e.getValue()))));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
		return badRequest(ErrorResponse.of(ErrorCode.VALIDATION_FAILED,
				List.of(new ErrorResponse.FieldError(e.getParameterName(), "필수 값입니다."))));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		log.debug("요청 본문 변환 실패: {}", e.getMostSpecificCause().getMessage());
		return badRequest(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, "요청 본문 형식이 올바르지 않습니다."));
	}

	@ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleUploadTooLarge(Exception e) {
		return badRequest(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, "이미지는 5MB 이하만 올릴 수 있습니다."));
	}

	@ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
	public ResponseEntity<ErrorResponse> handleMissingPart(Exception e) {
		return badRequest(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, "업로드할 이미지 파일을 선택해 주세요."));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
		return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus()).body(ErrorResponse.of(ErrorCode.NOT_FOUND));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
				.body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		log.error("처리되지 않은 예외", e);
		return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
				.body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
	}

	private ResponseEntity<ErrorResponse> badRequest(ErrorResponse body) {
		return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus()).body(body);
	}

}
