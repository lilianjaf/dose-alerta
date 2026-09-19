package com.dosealerta.scheduler.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RelatorioAdesaoClientConfig {

	@Bean
	public RestClient relatorioAdesaoRestClient(
			@Value("${modulo-relatorio-adesao.uri}") String baseUrl,
			@Value("${modulo-relatorio-adesao.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${modulo-relatorio-adesao.read-timeout-ms}") int readTimeoutMs,
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
