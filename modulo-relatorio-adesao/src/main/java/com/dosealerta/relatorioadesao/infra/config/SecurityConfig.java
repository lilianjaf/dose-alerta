package com.dosealerta.relatorioadesao.infra.config;

import com.dosealerta.relatorioadesao.infra.security.JwtAuthenticationFilter;
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
 * Defesa em profundidade (Etapa 10.1 / seção 2 do RESUMO_TECNICO.md). `POST /interacoes` é
 * acionado só pelo modulo-scheduler — hoje não existe, no RESUMO_TECNICO.md, um mecanismo de
 * identidade de serviço (só o JWT do paciente), então permanece sem exigir token, como já
 * documentado nas Etapas 6/8 para lacunas equivalentes. `GET /pacientes/{id}/adesao` é consulta
 * do profissional de saúde e expõe dado de saúde do paciente, por isso exige o mesmo JWT
 * validado no api-gateway.
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
						.requestMatchers(HttpMethod.POST, "/interacoes")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/prometheus")
						.permitAll()
						.anyRequest()
						.authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
