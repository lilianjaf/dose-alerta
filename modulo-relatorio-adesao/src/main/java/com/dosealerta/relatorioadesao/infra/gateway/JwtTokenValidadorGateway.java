package com.dosealerta.relatorioadesao.infra.gateway;

import com.dosealerta.relatorioadesao.core.gateway.TokenValidadorGateway;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class JwtTokenValidadorGateway implements TokenValidadorGateway {

	private static final Logger log = LoggerFactory.getLogger(JwtTokenValidadorGateway.class);
	private static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofSeconds(60);

	private final RSASSAVerifier verifier;

	JwtTokenValidadorGateway(RSAPublicKey jwtPublicKey) {
		this.verifier = new RSASSAVerifier(jwtPublicKey);
	}

	@Override
	public Optional<String> validarEExtrairIdentificador(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			if (!signedJWT.verify(verifier)) {
				log.debug("Assinatura de token JWT invalida");
				return Optional.empty();
			}
			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
			Date expiracao = claims.getExpirationTime();
			if (expiracao == null || expiracao.toInstant().plus(CLOCK_SKEW_TOLERANCE).isBefore(Instant.now())) {
				log.debug("Token JWT expirado");
				return Optional.empty();
			}
			return Optional.ofNullable(claims.getSubject());
		} catch (ParseException | JOSEException e) {
			log.debug("Token JWT malformado ou nao pode ser verificado: {}", e.getMessage());
			return Optional.empty();
		} catch (RuntimeException e) {
			log.warn("Erro inesperado ao validar token JWT", e);
			return Optional.empty();
		}
	}
}
