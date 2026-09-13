package com.rentdb.global.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.rentdb.global.error.ErrorCode;
import com.rentdb.global.error.ErrorResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * 보안 필터 단계(컨트롤러 도달 전)의 오류도 API와 같은 { code, message, fieldErrors } 형식으로 응답한다.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

	private final JsonMapper jsonMapper;

	public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		write(response, errorCode.getStatus().value(), jsonMapper.writeValueAsString(ErrorResponse.of(errorCode)));
	}

	public void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
		write(response, status, jsonMapper.writeValueAsString(body));
	}

	private void write(HttpServletResponse response, int status, String json) throws IOException {
		if (response.isCommitted()) {
			return;
		}
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(json);
	}

}
