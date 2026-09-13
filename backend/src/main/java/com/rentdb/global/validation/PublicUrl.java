package com.rentdb.global.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

/**
 * 화면에 링크·이미지로 들어가는 주소 — 사이트 내부 경로(/...) 또는 https:// 만 허용한다.
 * javascript: 같은 주소가 저장돼 관리자·사용자 화면에서 실행되는 것을 막는다. (빈 값은 허용 → 필수면 @NotBlank 함께 사용)
 */
@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^$|^/(?!/)[^\\s]*$|^https://[^\\s]+$", message = "주소는 / 로 시작하는 사이트 경로 또는 https:// 주소만 입력할 수 있습니다.")
public @interface PublicUrl {

	String message() default "주소는 / 로 시작하는 사이트 경로 또는 https:// 주소만 입력할 수 있습니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
