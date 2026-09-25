package com.dosealerta.scheduler.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	private static final String MDC_KEY = "correlationId";
	private static final Pattern CORRELATION_ID_VALIDO = Pattern.compile("[A-Za-z0-9_-]{1,100}");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || !CORRELATION_ID_VALIDO.matcher(correlationId).matches()) {
			correlationId = UUID.randomUUID().toString();
		}

		response.setHeader(CORRELATION_ID_HEADER, correlationId);
		MDC.put(MDC_KEY, correlationId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}
}
