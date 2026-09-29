package com.dosealerta.apigateway.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

	private static final String CABECALHO_RETRY_AFTER = "Retry-After";
	private static final String CABECALHO_X_FORWARDED_FOR = "X-Forwarded-For";
	private static final Set<String> ROTAS_ISENTAS = Set.of("/actuator/health", "/actuator/prometheus");

	private final long capacidade;
	private final long janelaMs;
	private final boolean confiarEmXForwardedFor;
	private final ConcurrentHashMap<String, Janela> contadoresPorCliente = new ConcurrentHashMap<>();
	private final Clock clock;
	private volatile long ultimaLimpeza;

	public RateLimitFilter(
			@Value("${app.rate-limit.capacidade:60}") long capacidade,
			@Value("${app.rate-limit.janela-ms:60000}") long janelaMs,
			@Value("${app.rate-limit.confiar-x-forwarded-for:false}") boolean confiarEmXForwardedFor,
			Clock clock) {
		this.capacidade = capacidade;
		this.janelaMs = janelaMs;
		this.confiarEmXForwardedFor = confiarEmXForwardedFor;
		this.clock = clock;
		this.ultimaLimpeza = clock.millis();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (ROTAS_ISENTAS.contains(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		removerJanelasExpiradas();
		String clienteId = identificarCliente(request);
		Janela janela = contadoresPorCliente.computeIfAbsent(clienteId, id -> new Janela(clock.millis()));

		if (janela.excedeuCapacidade(clock.millis(), janelaMs, capacidade)) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setHeader(CABECALHO_RETRY_AFTER, String.valueOf(janelaMs / 1000));
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String identificarCliente(HttpServletRequest request) {
		if (confiarEmXForwardedFor) {
			String encaminhado = request.getHeader(CABECALHO_X_FORWARDED_FOR);
			if (encaminhado != null && !encaminhado.isBlank()) {
				return encaminhado.split(",")[0].trim();
			}
		}
		return request.getRemoteAddr();
	}

	private void removerJanelasExpiradas() {
		long agora = clock.millis();
		if (agora - ultimaLimpeza < janelaMs) {
			return;
		}
		ultimaLimpeza = agora;
		contadoresPorCliente.values().removeIf(janela -> janela.expirou(agora, janelaMs));
	}

	private static final class Janela {
		private long inicio;
		private long contagem;

		Janela(long inicio) {
			this.inicio = inicio;
		}

		synchronized boolean expirou(long agora, long janelaMs) {
			return agora - inicio >= janelaMs;
		}

		synchronized boolean excedeuCapacidade(long agora, long janelaMs, long capacidade) {
			if (agora - inicio >= janelaMs) {
				inicio = agora;
				contagem = 0;
			}
			contagem++;
			return contagem > capacidade;
		}
	}
}
