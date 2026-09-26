package com.dosealerta.ia.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiClientConfig {

	@Bean
	public RestClient geminiRestClient(
			@Value("${ia.gemini.uri:https://generativelanguage.googleapis.com}") String baseUrl,
			@Value("${ia.gemini.api-key:}") String apiKey,
			@Value("${ia.gemini.connect-timeout-ms:3000}") int connectTimeoutMs,
			@Value("${ia.gemini.read-timeout-ms:30000}") int readTimeoutMs) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeoutMs);
		requestFactory.setReadTimeout(readTimeoutMs);

		return RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.defaultHeader("x-goog-api-key", apiKey)
				.build();
	}
}
