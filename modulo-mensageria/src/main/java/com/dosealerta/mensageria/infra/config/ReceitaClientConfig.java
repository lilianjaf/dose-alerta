package com.dosealerta.mensageria.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ReceitaClientConfig {

	@Bean
	public RestClient iaRestClient(
			@Value("${modulo-ia.uri}") String baseUrl,
			@Value("${modulo-ia.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${modulo-ia.read-timeout-ms}") int readTimeoutMs,
			CorrelationIdRequestInterceptor correlationIdRequestInterceptor) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeoutMs);

		requestFactory.setReadTimeout(readTimeoutMs);

		return RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.requestInterceptor(correlationIdRequestInterceptor)
				.build();
	}
}
