package com.rentdb.global.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 알림 발송 같은 후속 작업을 요청 스레드와 분리해 실행한다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	public static final String NOTIFICATION_EXECUTOR = "notificationExecutor";

	@Bean(name = NOTIFICATION_EXECUTOR)
	public Executor notificationExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("notify-");
		// 서버 종료 시 발송 중인 메일을 마무리할 시간을 준다
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.initialize();
		return executor;
	}

}
