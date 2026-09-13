package com.dosealerta.usuario.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.usuario.core.domain.Paciente;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtAutenticacaoGatewayTest {

	private JwtAutenticacaoGateway gateway;
	private RSAPublicKey outraChavePublica;

	@BeforeEach
	void setUp() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);

		KeyPair chaves = keyPairGenerator.generateKeyPair();
		gateway = new JwtAutenticacaoGateway(
				(RSAPrivateKey) chaves.getPrivate(), (RSAPublicKey) chaves.getPublic(), "modulo-usuario");

		outraChavePublica = (RSAPublicKey) keyPairGenerator.generateKeyPair().getPublic();
	}

	@Test
	void deveEmitirTokenValidoParaAMesmaChave() {
		Paciente paciente = Paciente.novo("Maria", "+5511999999999", "hash");

		String token = gateway.emitirToken(paciente);

		assertTrue(gateway.validarEExtrairIdentificador(token).isPresent());
	}

	@Test
	void deveRejeitarTokenAssinadoComOutraChave() throws Exception {
		Paciente paciente = Paciente.novo("Maria", "+5511999999999", "hash");
		String token = gateway.emitirToken(paciente);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var outroGatewayParaValidar = new JwtAutenticacaoGateway(
				(RSAPrivateKey) keyPairGenerator.generateKeyPair().getPrivate(), outraChavePublica, "modulo-usuario");

		assertTrue(outroGatewayParaValidar.validarEExtrairIdentificador(token).isEmpty());
	}

	@Test
	void deveRejeitarTokenMalformado() {
		assertTrue(gateway.validarEExtrairIdentificador("token-invalido").isEmpty());
	}
}
