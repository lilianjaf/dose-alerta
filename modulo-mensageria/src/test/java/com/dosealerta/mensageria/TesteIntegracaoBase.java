package com.dosealerta.mensageria;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public abstract class TesteIntegracaoBase {

	protected static final String AUTH_TOKEN = "test-auth-token";
	protected static final String URL_BASE_WEBHOOK = "http://localhost:8085";

	private static final String ALGORITMO_CHAVE = "RSA";
	private static final int TAMANHO_CHAVE = 2048;

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("twilio.account-sid", () -> "ACtest0000000000000000000000000000");
		registry.add("twilio.auth-token", () -> AUTH_TOKEN);
		registry.add("twilio.whatsapp-number", () -> MensageriaFixtures.TELEFONE);
		registry.add("twilio.voice-number", () -> MensageriaFixtures.TELEFONE);
		registry.add("twilio.webhook-base-url", () -> URL_BASE_WEBHOOK);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITMO_CHAVE);
		keyPairGenerator.initialize(TAMANHO_CHAVE);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
	}

	@BeforeEach
	protected void limparMdcAntes() {
		MDC.clear();
	}

	@AfterEach
	protected void limparMdcDepois() {
		MDC.clear();
	}
}
