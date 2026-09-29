package com.dosealerta.apigateway.infra.gateway;

import static com.dosealerta.apigateway.ApiGatewayFixtures.CLOCK_FIXO;
import static com.dosealerta.apigateway.ApiGatewayFixtures.INSTANTE_FIXO;
import static com.dosealerta.apigateway.ApiGatewayFixtures.tokenAssinadoCom;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.apigateway.TesteUnitarioBase;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenValidadorGatewayTest extends TesteUnitarioBase {

	private JwtTokenValidadorGateway gateway;
	private RSAPrivateKey chavePrivada;

	@BeforeEach
	void setUp() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		chavePrivada = (RSAPrivateKey) chaves.getPrivate();
		gateway = new JwtTokenValidadorGateway((RSAPublicKey) chaves.getPublic(), CLOCK_FIXO);
	}

	private String assinar(RSAPrivateKey chave, Instant expiracao) throws Exception {
		return tokenAssinadoCom(chave, expiracao);
	}

	@Test
	void deveAceitarTokenAssinadoComAChaveCorrespondente() throws Exception {
		String token = assinar(chavePrivada, INSTANTE_FIXO.plusSeconds(3600));

		assertTrue(gateway.validarEExtrairIdentificador(token).isPresent());
	}

	@Test
	void deveRejeitarTokenAssinadoComOutraChave() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		RSAPrivateKey outraChavePrivada = (RSAPrivateKey) keyPairGenerator.generateKeyPair().getPrivate();
		String token = assinar(outraChavePrivada, INSTANTE_FIXO.plusSeconds(3600));

		assertTrue(gateway.validarEExtrairIdentificador(token).isEmpty());
	}

	@Test
	void deveRejeitarTokenExpirado() throws Exception {
		String token = assinar(chavePrivada, INSTANTE_FIXO.minusSeconds(3600));

		assertTrue(gateway.validarEExtrairIdentificador(token).isEmpty());
	}

	@Test
	void deveAceitarTokenExpiradoDentroDaToleranciaDeClockSkew() throws Exception {
		String token = assinar(chavePrivada, INSTANTE_FIXO.minusSeconds(1));

		assertTrue(gateway.validarEExtrairIdentificador(token).isPresent());
	}

	@Test
	void deveRejeitarTokenMalformado() {
		assertTrue(gateway.validarEExtrairIdentificador("token-invalido").isEmpty());
	}
}
