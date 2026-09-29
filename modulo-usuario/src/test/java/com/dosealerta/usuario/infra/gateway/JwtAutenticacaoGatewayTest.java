package com.dosealerta.usuario.infra.gateway;

import static com.dosealerta.usuario.UsuarioFixtures.CLOCK_FIXO;
import static com.dosealerta.usuario.UsuarioFixtures.umPaciente;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.domain.Paciente;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtAutenticacaoGatewayTest extends TesteUnitarioBase {

	private static final String ISSUER = "modulo-usuario";
	private static final String ALGORITMO_CHAVE = "RSA";
	private static final int TAMANHO_CHAVE = 2048;
	private static final String TOKEN_MALFORMADO = "token-invalido";
	private static final Duration MAIS_QUE_A_VALIDADE = Duration.ofHours(13);

	private KeyPairGenerator keyPairGenerator;
	private KeyPair chaves;
	private JwtAutenticacaoGateway gateway;
	private Paciente paciente;

	@BeforeEach
	void setUp() throws Exception {
		keyPairGenerator = KeyPairGenerator.getInstance(ALGORITMO_CHAVE);
		keyPairGenerator.initialize(TAMANHO_CHAVE);
		chaves = keyPairGenerator.generateKeyPair();
		gateway = gatewayCom(chaves, CLOCK_FIXO);
		paciente = umPaciente();
	}

	private JwtAutenticacaoGateway gatewayCom(KeyPair par, Clock clock) {
		return new JwtAutenticacaoGateway(
				(RSAPrivateKey) par.getPrivate(), (RSAPublicKey) par.getPublic(), ISSUER, clock);
	}

	@Test
	void deveEmitirTokenValidoParaAMesmaChaveComOIdentificadorDoPaciente() {
		String token = gateway.emitirToken(paciente);

		assertEquals(paciente.getId().toString(), gateway.validarEExtrairIdentificador(token).orElseThrow());
	}

	@Test
	void deveRejeitarTokenAssinadoComOutraChave() {
		String token = gateway.emitirToken(paciente);
		JwtAutenticacaoGateway outroGateway = gatewayCom(keyPairGenerator.generateKeyPair(), CLOCK_FIXO);

		assertTrue(outroGateway.validarEExtrairIdentificador(token).isEmpty());
	}

	@Test
	void deveRejeitarTokenExpirado() {
		String token = gateway.emitirToken(paciente);
		JwtAutenticacaoGateway gatewayNoFuturo = gatewayCom(chaves, Clock.offset(CLOCK_FIXO, MAIS_QUE_A_VALIDADE));

		assertTrue(gatewayNoFuturo.validarEExtrairIdentificador(token).isEmpty());
	}

	@Test
	void deveRejeitarTokenMalformado() {
		assertTrue(gateway.validarEExtrairIdentificador(TOKEN_MALFORMADO).isEmpty());
	}
}
