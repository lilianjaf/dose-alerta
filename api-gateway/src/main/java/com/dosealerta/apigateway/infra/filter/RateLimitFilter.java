package com.dosealerta.apigateway.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting por IP na borda (Etapa 10.2), janela fixa em memória. O rate limiter nativo do
 * Spring Cloud Gateway Server WebMVC (Bucket4j) só se pluga via {@code RouterFunction}
 * programático — este projeto roteia via YAML declarativo (seção 2.1), então um filtro no
 * mesmo estilo do {@link CorrelationIdFilter}/{@code JwtValidationFilter} já existentes é mais
 * simples e consistente do que reestruturar o roteamento só para reaproveitar aquele filtro.
 *
 * <p>Roda antes dos demais filtros (@Order(1)) para rejeitar tráfego abusivo o quanto antes.
 * `/actuator/health` e `/actuator/prometheus` ficam de fora — são polling de infraestrutura
 * (orquestrador de containers, Prometheus), não tráfego de cliente. Em memória e por instância:
 * suficiente para uma única réplica do gateway (o estágio atual do projeto); não substitui um
 * WAF/CDN com rate limiting distribuído quando houver múltiplas réplicas atrás de um LB.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

	private static final Set<String> ROTAS_ISENTAS = Set.of("/actuator/health", "/actuator/prometheus");

	private final long capacidade;
	private final long janelaMs;
	private final boolean confiarEmXForwardedFor;
	private final ConcurrentHashMap<String, Janela> contadoresPorCliente = new ConcurrentHashMap<>();
	private volatile long ultimaLimpeza = System.currentTimeMillis();

	public RateLimitFilter(
			@Value("${app.rate-limit.capacidade:60}") long capacidade,
			@Value("${app.rate-limit.janela-ms:60000}") long janelaMs,
			@Value("${app.rate-limit.confiar-x-forwarded-for:false}") boolean confiarEmXForwardedFor) {
		this.capacidade = capacidade;
		this.janelaMs = janelaMs;
		this.confiarEmXForwardedFor = confiarEmXForwardedFor;
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
		Janela janela = contadoresPorCliente.computeIfAbsent(clienteId, id -> new Janela());

		if (janela.excedeuCapacidade(janelaMs, capacidade)) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setHeader("Retry-After", String.valueOf(janelaMs / 1000));
			return;
		}

		filterChain.doFilter(request, response);
	}

	/** Só confia no X-Forwarded-For quando o gateway está atrás de um proxy que o sobrescreve (spoofável caso contrário). */
	private String identificarCliente(HttpServletRequest request) {
		if (confiarEmXForwardedFor) {
			String encaminhado = request.getHeader("X-Forwarded-For");
			if (encaminhado != null && !encaminhado.isBlank()) {
				return encaminhado.split(",")[0].trim();
			}
		}
		return request.getRemoteAddr();
	}

	private void removerJanelasExpiradas() {
		long agora = System.currentTimeMillis();
		if (agora - ultimaLimpeza < janelaMs) {
			return;
		}
		ultimaLimpeza = agora;
		contadoresPorCliente.values().removeIf(janela -> janela.expirou(agora, janelaMs));
	}

	private static final class Janela {
		private long inicio = System.currentTimeMillis();
		private long contagem;

		synchronized boolean expirou(long agora, long janelaMs) {
			return agora - inicio >= janelaMs;
		}

		synchronized boolean excedeuCapacidade(long janelaMs, long capacidade) {
			long agora = System.currentTimeMillis();
			if (agora - inicio >= janelaMs) {
				inicio = agora;
				contagem = 0;
			}
			contagem++;
			return contagem > capacidade;
		}
	}
}
