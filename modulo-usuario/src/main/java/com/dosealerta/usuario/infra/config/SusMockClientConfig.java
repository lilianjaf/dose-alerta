package com.dosealerta.usuario.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SusMockClientConfig {

	@Bean
	public RestClient susMockRestClient(
			@Value("${modulo-sus-mock.uri}") String baseUrl,
			@Value("${modulo-sus-mock.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${modulo-sus-mock.read-timeout-ms}") int readTimeoutMs,
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
