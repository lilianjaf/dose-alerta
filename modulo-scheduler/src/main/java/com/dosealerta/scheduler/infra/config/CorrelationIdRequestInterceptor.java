package com.dosealerta.scheduler.infra.config;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Propaga o correlation-id atual (do MDC — populado por {@code CorrelationIdFilter} para
 * chamadas síncronas, ou restaurado pelo publisher do outbox a partir do evento persistido
 * para chamadas assíncronas) como header nas chamadas HTTP a outros módulos (Etapa 9.1).
 */
@Component
class CorrelationIdRequestInterceptor implements ClientHttpRequestInterceptor {

	private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	private static final String MDC_KEY = "correlationId";

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		String correlationId = MDC.get(MDC_KEY);
		if (correlationId != null) {
			request.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
		}
		return execution.execute(request, body);
	}
}
