package com.dosealerta.mensageria.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class PacienteClientConfig {

	@Bean
	public RestClient usuarioRestClient(
			@Value("${modulo-usuario.uri}") String baseUrl,
			@Value("${modulo-usuario.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${modulo-usuario.read-timeout-ms}") int readTimeoutMs,
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
