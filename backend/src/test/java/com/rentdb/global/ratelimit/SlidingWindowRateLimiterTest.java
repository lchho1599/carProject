package com.rentdb.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

	@Test
	void 한도를_넘으면_막고_시간이_지나면_다시_허용한다() {
		MutableClock clock = new MutableClock();
		SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(clock, 2, 10);

		assertThat(limiter.tryAcquire("ip-a")).isTrue();
		assertThat(limiter.tryAcquire("ip-a")).isTrue();
		assertThat(limiter.tryAcquire("ip-a")).isFalse();
		assertThat(limiter.tryAcquire("ip-b")).as("다른 IP는 따로 계산").isTrue();

		clock.advance(Duration.ofMinutes(10).plusSeconds(1));
		assertThat(limiter.tryAcquire("ip-a")).isTrue();
	}

	private static final class MutableClock extends Clock {

		private Instant now = Instant.parse("2026-09-13T00:00:00Z");

		void advance(Duration duration) {
			now = now.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneId.of("Asia/Seoul");
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}

	}

}
