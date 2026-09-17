package com.dosealerta.ia.infra.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicClientConfig {

	/**
	 * Lê as credenciais do ambiente (ordem padrão do SDK: {@code ANTHROPIC_API_KEY},
	 * {@code ANTHROPIC_AUTH_TOKEN}, perfil OAuth ativo) — ver seção "IA (modulo-ia)" do
	 * README para como configurar em desenvolvimento.
	 */
	@Bean
	public AnthropicClient anthropicClient() {
		return AnthropicOkHttpClient.fromEnv();
	}
}
