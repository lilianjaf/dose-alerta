package com.dosealerta.notificacao.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class MensageriaClientConfig {

	@Bean
	public RestClient mensageriaRestClient(
			@Value("${modulo-mensageria.uri}") String baseUrl,
			@Value("${modulo-mensageria.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${modulo-mensageria.read-timeout-ms}") int readTimeoutMs) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeoutMs);
		requestFactory.setReadTimeout(readTimeoutMs);

		return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
	}
}
