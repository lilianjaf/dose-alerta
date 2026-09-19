package com.dosealerta.scheduler.infra.config;

import com.dosealerta.scheduler.infra.security.JwtAuthenticationFilter;
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
 * Defesa em profundidade (Etapa 10.1 / seção 2 do RESUMO_TECNICO.md). `POST /alarmes` (acionado
 * pelo modulo-ia) e `POST /alarmes/confirmacoes` / `POST /alarmes/ligacoes/atendidas` (acionados
 * pelo modulo-mensageria em resposta a um webhook Twilio) são chamadas módulo-a-módulo — hoje
 * não existe, no RESUMO_TECNICO.md, um mecanismo de identidade de serviço (só o JWT do
 * paciente), então permanecem sem exigir token, como já documentado nas Etapas 6/8 para lacunas
 * equivalentes. `GET /alarmes/{id}` não tem nenhum chamador módulo-a-módulo hoje e expõe dado de
 * saúde do paciente, por isso passa a exigir o mesmo JWT validado no api-gateway.
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
						.requestMatchers(
								HttpMethod.POST, "/alarmes", "/alarmes/confirmacoes", "/alarmes/ligacoes/atendidas")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/prometheus")
						.permitAll()
						.anyRequest()
						.authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
