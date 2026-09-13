package com.rentdb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 로컬 DB(docker compose, 5433)가 실행 중이어야 통과한다.
 */
@SpringBootTest
class RentDbApplicationTests {

	@Test
	void contextLoads() {
	}

}
