package com.dosealerta.scheduler.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NotificacaoClientConfig {

	@Bean
	public RestClient notificacaoRestClient(@Value("${modulo-notificacao.uri}") String baseUrl) {
		return RestClient.builder().baseUrl(baseUrl).build();
	}
}
