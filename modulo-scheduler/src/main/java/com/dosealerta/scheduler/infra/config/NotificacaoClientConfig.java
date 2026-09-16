package com.dosealerta.scheduler.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class NotificacaoClientConfig {

	@Bean
	public RestClient notificacaoRestClient(
			@Value("${modulo-notificacao.uri}") String baseUrl,
			@Value("${modulo-notificacao.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${modulo-notificacao.read-timeout-ms}") int readTimeoutMs) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeoutMs);
		requestFactory.setReadTimeout(readTimeoutMs);

		return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
	}
}
