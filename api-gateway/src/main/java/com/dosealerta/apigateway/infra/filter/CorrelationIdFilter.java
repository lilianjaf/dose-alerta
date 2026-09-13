package com.dosealerta.apigateway.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gera (ou reaproveita) um correlation-id por requisição, devolve no header de resposta e
 * propaga para os módulos internos via header da requisição roteada (seção 7 do
 * RESUMO_TECNICO.md — usado desde já, mesmo sem tracing completo ainda).
 */
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	private static final String MDC_KEY = "correlationId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		HttpServletRequest requisicaoParaEncadear = request;

		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
			requisicaoParaEncadear = new RequisicaoComCorrelationId(request, correlationId);
		}

		response.setHeader(CORRELATION_ID_HEADER, correlationId);
		MDC.put(MDC_KEY, correlationId);
		try {
			filterChain.doFilter(requisicaoParaEncadear, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}

	private static final class RequisicaoComCorrelationId extends HttpServletRequestWrapper {

		private final String correlationId;

		RequisicaoComCorrelationId(HttpServletRequest request, String correlationId) {
			super(request);
			this.correlationId = correlationId;
		}

		@Override
		public String getHeader(String name) {
			if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
				return correlationId;
			}
			return super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
				return Collections.enumeration(List.of(correlationId));
			}
			return super.getHeaders(name);
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			List<String> nomes = new ArrayList<>(Collections.list(super.getHeaderNames()));
			if (!nomes.contains(CORRELATION_ID_HEADER)) {
				nomes.add(CORRELATION_ID_HEADER);
			}
			return Collections.enumeration(nomes);
		}
	}
}
