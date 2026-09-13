package com.dosealerta.apigateway.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenValidadorGatewayTest {

	private JwtTokenValidadorGateway gateway;
	private RSAPrivateKey chavePrivada;

	@BeforeEach
	void setUp() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		chavePrivada = (RSAPrivateKey) chaves.getPrivate();
		gateway = new JwtTokenValidadorGateway((RSAPublicKey) chaves.getPublic());
	}

	private String assinar(RSAPrivateKey chave, Instant expiracao) throws Exception {
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject("paciente-1")
				.issueTime(Date.from(Instant.now()))
				.expirationTime(Date.from(expiracao))
				.build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		signedJWT.sign(new RSASSASigner(chave));
		return signedJWT.serialize();
	}

	@Test
	void deveAceitarTokenAssinadoComAChaveCorrespondente() throws Exception {
		String token = assinar(chavePrivada, Instant.now().plusSeconds(3600));

		assertTrue(gateway.validarEExtrairIdentificador(token).isPresent());
	}

	@Test
	void deveRejeitarTokenAssinadoComOutraChave() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		RSAPrivateKey outraChavePrivada = (RSAPrivateKey) keyPairGenerator.generateKeyPair().getPrivate();
		String token = assinar(outraChavePrivada, Instant.now().plusSeconds(3600));

		assertTrue(gateway.validarEExtrairIdentificador(token).isEmpty());
	}

	@Test
	void deveRejeitarTokenExpirado() throws Exception {
		String token = assinar(chavePrivada, Instant.now().minusSeconds(3600));

		assertTrue(gateway.validarEExtrairIdentificador(token).isEmpty());
	}

	@Test
	void deveAceitarTokenExpiradoDentroDaToleranciaDeClockSkew() throws Exception {
		String token = assinar(chavePrivada, Instant.now().minusSeconds(1));

		assertTrue(gateway.validarEExtrairIdentificador(token).isPresent());
	}

	@Test
	void deveRejeitarTokenMalformado() {
		assertTrue(gateway.validarEExtrairIdentificador("token-invalido").isEmpty());
	}
}
