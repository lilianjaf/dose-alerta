package com.dosealerta.mensageria.infra.gateway;

import com.dosealerta.mensageria.core.gateway.TokenValidadorGateway;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class JwtTokenValidadorGateway implements TokenValidadorGateway {

	private static final String LOG_ASSINATURA_INVALIDA = "Assinatura de token JWT invalida";
	private static final String LOG_TOKEN_EXPIRADO = "Token JWT expirado";
	private static final String LOG_TOKEN_MALFORMADO = "Token JWT malformado ou nao pode ser verificado: {}";
	private static final String LOG_ERRO_INESPERADO = "Erro inesperado ao validar token JWT";
	private static final Logger log = LoggerFactory.getLogger(JwtTokenValidadorGateway.class);
	private static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofSeconds(60);

	private final RSASSAVerifier verifier;
	private final Clock clock;

	JwtTokenValidadorGateway(RSAPublicKey jwtPublicKey, Clock clock) {
		this.verifier = new RSASSAVerifier(jwtPublicKey);
		this.clock = clock;
	}

	@Override
	public Optional<String> validarEExtrairIdentificador(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			if (!signedJWT.verify(verifier)) {
				log.debug(LOG_ASSINATURA_INVALIDA);
				return Optional.empty();
			}
			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
			Date expiracao = claims.getExpirationTime();
			if (expiracao == null || expiracao.toInstant().plus(CLOCK_SKEW_TOLERANCE).isBefore(clock.instant())) {
				log.debug(LOG_TOKEN_EXPIRADO);
				return Optional.empty();
			}
			return Optional.ofNullable(claims.getSubject());
		} catch (ParseException | JOSEException e) {
			log.debug(LOG_TOKEN_MALFORMADO, e.getMessage());
			return Optional.empty();
		} catch (RuntimeException e) {
			log.warn(LOG_ERRO_INESPERADO, e);
			return Optional.empty();
		}
	}
}
