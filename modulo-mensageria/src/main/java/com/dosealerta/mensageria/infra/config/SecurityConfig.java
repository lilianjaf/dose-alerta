package com.dosealerta.mensageria.infra.config;

import com.dosealerta.mensageria.infra.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Defesa em profundidade (Etapa 10.1 / seção 2 do RESUMO_TECNICO.md). `POST /mensagens/enviar` e
 * `POST /ligacoes/realizar` são acionados só pelo modulo-notificacao — hoje não existe, no
 * RESUMO_TECNICO.md, um mecanismo de identidade de serviço (só o JWT do paciente), então
 * permanecem sem exigir token, como já documentado nas Etapas 6/8 para lacunas equivalentes.
 * Os webhooks `/webhooks/twilio/**` nunca terão um JWT de paciente (quem chama é a Twilio) — a
 * defesa deles é a assinatura `X-Twilio-Signature` (Etapa 10.5), não este filtro.
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
			throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/mensagens/enviar", "/ligacoes/realizar")
						.permitAll()
						.requestMatchers(
								HttpMethod.POST,
								"/webhooks/twilio/mensagens",
								"/webhooks/twilio/ligacoes/confirmacao",
								"/webhooks/twilio/ligacoes/status")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/prometheus")
						.permitAll()
						.anyRequest()
						.authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
