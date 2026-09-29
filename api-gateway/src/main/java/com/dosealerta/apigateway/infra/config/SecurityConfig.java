package com.dosealerta.apigateway.infra.config;

import com.dosealerta.apigateway.core.gateway.TokenValidadorGateway;
import com.dosealerta.apigateway.infra.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private static final String[] ROTAS_PUBLICAS_POST = {
		"/pacientes",
		"/auth/login",
		"/webhooks/twilio/mensagens",
		"/webhooks/twilio/ligacoes/confirmacao",
		"/webhooks/twilio/ligacoes/status"
	};
	private static final String[] ROTAS_PUBLICAS_GET = {"/actuator/health", "/actuator/prometheus"};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, TokenValidadorGateway tokenValidadorGateway)
			throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth.dispatcherTypeMatchers(DispatcherType.ERROR)
						.permitAll()
						.requestMatchers(HttpMethod.OPTIONS)
						.permitAll()
						.requestMatchers(HttpMethod.POST, ROTAS_PUBLICAS_POST)
						.permitAll()
						.requestMatchers(HttpMethod.GET, ROTAS_PUBLICAS_GET)
						.permitAll()
						.anyRequest()
						.authenticated())
				.addFilterBefore(
						new JwtAuthenticationFilter(tokenValidadorGateway), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
