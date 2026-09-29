package com.dosealerta.scheduler;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@Import(RelogioDeTesteConfig.class)
public abstract class TesteIntegracaoBase {

	private static final String IMAGEM_POSTGRES = "postgres:16-alpine";
	private static final String ALGORITMO_CHAVE = "RSA";
	private static final int TAMANHO_CHAVE = 2048;
	private static final String SUBJECT_TOKEN = "paciente-1";
	private static final Duration VALIDADE_TOKEN = Duration.ofHours(1);
	private static final String CONSULTA_TABELAS =
			"SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'";
	private static final String TRUNCAR_TABELAS = "TRUNCATE TABLE %s CASCADE";

	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(IMAGEM_POSTGRES);

	static {
		postgres.start();
	}

	private static RSAPrivateKey chavePrivada;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	protected RelogioDeTeste relogio;

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITMO_CHAVE);
		keyPairGenerator.initialize(TAMANHO_CHAVE);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		chavePrivada = (RSAPrivateKey) chaves.getPrivate();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
	}

	@BeforeEach
	protected void reiniciarRelogio() {
		relogio.definir(SchedulerFixtures.INSTANTE_FIXO);
	}

	@AfterEach
	protected void limparBanco() {
		List<String> tabelas = jdbcTemplate.queryForList(CONSULTA_TABELAS, String.class);
		if (!tabelas.isEmpty()) {
			jdbcTemplate.execute(TRUNCAR_TABELAS.formatted(String.join(", ", tabelas)));
		}
	}

	protected static String tokenValido() throws Exception {
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject(SUBJECT_TOKEN)
				.issueTime(Date.from(SchedulerFixtures.INSTANTE_FIXO))
				.expirationTime(Date.from(SchedulerFixtures.INSTANTE_FIXO.plus(VALIDADE_TOKEN)))
				.build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		signedJWT.sign(new RSASSASigner(chavePrivada));
		return signedJWT.serialize();
	}
}
