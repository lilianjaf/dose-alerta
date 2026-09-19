package com.dosealerta.mensageria.infra.security;

import com.twilio.security.RequestValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Valida a assinatura {@code X-Twilio-Signature} nos webhooks do Twilio (Etapa 10.5 — achado de
 * code review da Etapa 3, deixado para cá deliberadamente). Sem isso, qualquer um que descubra a
 * URL do webhook pode forjar uma confirmação de alarme ou resposta de paciente — os únicos
 * endpoints deste módulo abertos a chamadores externos (não módulo-a-módulo), então são os
 * únicos que realmente precisam de uma defesa contra origem forjada.
 */
@Component
public class TwilioSignatureFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(TwilioSignatureFilter.class);
	private static final String SIGNATURE_HEADER = "X-Twilio-Signature";
	private static final Set<String> ROTAS_WEBHOOK = Set.of(
			"/webhooks/twilio/mensagens", "/webhooks/twilio/ligacoes/confirmacao", "/webhooks/twilio/ligacoes/status");

	private final RequestValidator requestValidator;
	private final String webhookBaseUrl;

	public TwilioSignatureFilter(
			@Value("${twilio.auth-token}") String authToken,
			@Value("${twilio.webhook-base-url}") String webhookBaseUrl) {
		if (webhookBaseUrl.isBlank()) {
			throw new IllegalStateException("twilio.webhook-base-url (TWILIO_WEBHOOK_BASE_URL) nao pode ser vazio");
		}
		this.requestValidator = new RequestValidator(authToken);
		this.webhookBaseUrl =
				webhookBaseUrl.endsWith("/") ? webhookBaseUrl.substring(0, webhookBaseUrl.length() - 1) : webhookBaseUrl;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!ROTAS_WEBHOOK.contains(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		String assinatura = request.getHeader(SIGNATURE_HEADER);
		String queryString = request.getQueryString();
		// O Twilio assina a URL completa (com query string) + apenas os parametros do corpo do POST.
		String url = webhookBaseUrl + request.getRequestURI() + (queryString != null ? "?" + queryString : "");
		Set<String> chavesDaQuery = queryString == null
				? Set.of()
				: UriComponentsBuilder.newInstance().query(queryString).build().getQueryParams().keySet();
		Map<String, String> parametros = new LinkedHashMap<>();
		request.getParameterMap().forEach((chave, valores) -> {
			if (valores.length > 0 && !chavesDaQuery.contains(chave)) {
				parametros.put(chave, valores[0]);
			}
		});

		if (assinatura == null || !requestValidator.validate(url, parametros, assinatura)) {
			log.warn("Assinatura X-Twilio-Signature invalida ou ausente para {}", request.getRequestURI());
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		filterChain.doFilter(request, response);
	}
}
