package com.dosealerta.apigateway;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.RSAPrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

public final class ApiGatewayFixtures {

	public static final Instant INSTANTE_FIXO = Instant.parse("2026-01-15T12:00:00Z");
	public static final Clock CLOCK_FIXO = Clock.fixed(INSTANTE_FIXO, ZoneId.of("America/Sao_Paulo"));
	public static final Duration UMA_HORA = Duration.ofHours(1);

	private static final String SUBJECT = "paciente-1";

	private ApiGatewayFixtures() {
	}

	public static String tokenAssinadoCom(RSAPrivateKey chave, Instant expiracao) throws Exception {
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject(SUBJECT)
				.issueTime(Date.from(INSTANTE_FIXO))
				.expirationTime(Date.from(expiracao))
				.build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		signedJWT.sign(new RSASSASigner(chave));
		return signedJWT.serialize();
	}

	public static String tokenValidoAssinadoCom(RSAPrivateKey chave) throws Exception {
		return tokenAssinadoCom(chave, INSTANTE_FIXO.plus(UMA_HORA));
	}
}
