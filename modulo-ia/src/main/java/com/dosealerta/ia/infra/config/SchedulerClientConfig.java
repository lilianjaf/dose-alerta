package com.dosealerta.ia.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SchedulerClientConfig {

	@Bean
	public RestClient schedulerRestClient(
			@Value("${modulo-scheduler.uri}") String baseUrl,
			@Value("${modulo-scheduler.connect-timeout-ms}") int connectTimeoutMs,
			@Value("${modulo-scheduler.read-timeout-ms}") int readTimeoutMs) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeoutMs);
		requestFactory.setReadTimeout(readTimeoutMs);

		return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
	}
}
