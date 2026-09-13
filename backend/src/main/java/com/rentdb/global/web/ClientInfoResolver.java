package com.rentdb.global.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientInfoResolver {

	private static final int USER_AGENT_MAX_LENGTH = 500;

	private final String ipHashSalt;

	public ClientInfoResolver(@Value("${app.security.ip-hash-salt}") String ipHashSalt) {
		this.ipHashSalt = ipHashSalt;
	}

	public ClientInfo resolve(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent != null && userAgent.length() > USER_AGENT_MAX_LENGTH) {
			userAgent = userAgent.substring(0, USER_AGENT_MAX_LENGTH);
		}
		return new ClientInfo(hash(clientIp(request)), userAgent);
	}

	/**
	 * 운영: Cloudflare 프록시가 넣는 CF-Connecting-IP → X-Forwarded-For 첫 값 → 접속 주소 순서로 사용.
	 * 헤더는 위조될 수 있으므로 요청 제한 용도로만 쓰고 보안 판단에는 쓰지 않는다.
	 */
	private String clientIp(HttpServletRequest request) {
		String cfIp = request.getHeader("CF-Connecting-IP");
		if (cfIp != null && !cfIp.isBlank()) {
			return cfIp.strip();
		}
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].strip();
		}
		return request.getRemoteAddr();
	}

	private String hash(String ip) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest((ipHashSalt + ":" + ip).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(bytes);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
		}
	}

}
