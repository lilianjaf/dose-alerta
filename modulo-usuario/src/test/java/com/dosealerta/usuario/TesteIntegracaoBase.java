package com.dosealerta.usuario;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public abstract class TesteIntegracaoBase {

	private static final String IMAGEM_POSTGRES = "postgres:16-alpine";
	private static final String ALGORITMO_CHAVE = "RSA";
	private static final int TAMANHO_CHAVE = 2048;
	private static final String CONSULTA_TABELAS =
			"SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'";
	private static final String TRUNCAR_TABELAS = "TRUNCATE TABLE %s CASCADE";

	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(IMAGEM_POSTGRES);

	static {
		postgres.start();
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITMO_CHAVE);
		keyPairGenerator.initialize(TAMANHO_CHAVE);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
		registry.add(
				"security.jwt.private-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPrivate().getEncoded()));
	}

	@AfterEach
	protected void limparBanco() {
		List<String> tabelas = jdbcTemplate.queryForList(CONSULTA_TABELAS, String.class);
		if (!tabelas.isEmpty()) {
			jdbcTemplate.execute(TRUNCAR_TABELAS.formatted(String.join(", ", tabelas)));
		}
	}
}
