package com.dosealerta.scheduler.infra.security;

import com.dosealerta.scheduler.core.gateway.TokenValidadorGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PAPEL_PACIENTE = "ROLE_PACIENTE";
	private static final String BEARER_PREFIX = "Bearer ";

	private final TokenValidadorGateway tokenValidadorGateway;

	public JwtAuthenticationFilter(TokenValidadorGateway tokenValidadorGateway) {
		this.tokenValidadorGateway = tokenValidadorGateway;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header != null && header.startsWith(BEARER_PREFIX)) {
			String token = header.substring(BEARER_PREFIX.length());
			tokenValidadorGateway.validarEExtrairIdentificador(token).ifPresent(identificador -> {
				var authentication = new UsernamePasswordAuthenticationToken(
						identificador, null, List.of(new SimpleGrantedAuthority(PAPEL_PACIENTE)));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			});
		}

		filterChain.doFilter(request, response);
	}
}
