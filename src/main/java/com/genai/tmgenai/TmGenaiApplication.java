package com.genai.tmgenai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class TmGenaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmGenaiApplication.class, args);
	}

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5); // Set the number of core threads
		executor.setMaxPoolSize(10); // Set the maximum number of threads
		executor.setQueueCapacity(25); // Set the queue capacity
		executor.setThreadNamePrefix("AsyncThread-"); // Set thread name prefix
		executor.initialize();
		return executor;
	}
}


