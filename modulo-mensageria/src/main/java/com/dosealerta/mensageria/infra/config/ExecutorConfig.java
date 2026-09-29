package com.dosealerta.mensageria.infra.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

	@Bean
	public Executor executorMensagemRecebida() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("mensagem-recebida-");
		executor.initialize();
		return executor;
	}
}
