package com.dosealerta.apigateway.infra.security;

import com.dosealerta.apigateway.core.gateway.TokenValidadorGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(2)
public class JwtValidationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private static final Set<RotaPublica> ROTAS_PUBLICAS = Set.of(
			new RotaPublica("POST", "/pacientes"),
			new RotaPublica("POST", "/auth/login"),
			new RotaPublica("GET", "/actuator/health"),
			new RotaPublica("GET", "/actuator/prometheus"));

	private final TokenValidadorGateway tokenValidadorGateway;

	public JwtValidationFilter(TokenValidadorGateway tokenValidadorGateway) {
		this.tokenValidadorGateway = tokenValidadorGateway;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (isRotaPublica(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = extrairToken(request);
		if (token == null || tokenValidadorGateway.validarEExtrairIdentificador(token).isEmpty()) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isRotaPublica(HttpServletRequest request) {
		return ROTAS_PUBLICAS.contains(new RotaPublica(request.getMethod(), request.getRequestURI()));
	}

	private String extrairToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			return header.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	private record RotaPublica(String metodo, String caminho) {}
}
