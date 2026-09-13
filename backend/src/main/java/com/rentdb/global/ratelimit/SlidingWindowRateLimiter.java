package com.rentdb.global.ratelimit;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 키(IP 해시 등)당 요청 횟수 제한 — 슬라이딩 윈도우, 인스턴스 메모리 기준.
 * 서버가 여러 대로 늘면 인스턴스마다 따로 계산된다 (MVP 허용 범위, 운영에서는 Cloudflare 요청 제한 규칙과 병행).
 */
public class SlidingWindowRateLimiter {

	private static final int CLEANUP_THRESHOLD = 10_000;

	private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
	private final Clock clock;
	private final int maxRequests;
	private final long windowMillis;

	public SlidingWindowRateLimiter(Clock clock, int maxRequests, int windowMinutes) {
		this.clock = clock;
		this.maxRequests = maxRequests;
		this.windowMillis = windowMinutes * 60_000L;
	}

	/** 허용되면 기록하고 true, 한도를 넘으면 false */
	public boolean tryAcquire(String key) {
		long now = clock.millis();
		Deque<Long> timestamps = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
		synchronized (timestamps) {
			while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - windowMillis) {
				timestamps.pollFirst();
			}
			if (timestamps.size() >= maxRequests) {
				return false;
			}
			timestamps.addLast(now);
		}
		cleanupIfLarge(now);
		return true;
	}

	/** 오래된 키가 쌓이지 않도록 가끔 정리한다 */
	private void cleanupIfLarge(long now) {
		if (hits.size() < CLEANUP_THRESHOLD) {
			return;
		}
		hits.entrySet().removeIf(entry -> {
			Deque<Long> timestamps = entry.getValue();
			synchronized (timestamps) {
				Long last = timestamps.peekLast();
				return last == null || last <= now - windowMillis;
			}
		});
	}

}
