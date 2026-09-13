package com.dosealerta.apigateway.infra.gateway;

import com.dosealerta.apigateway.core.gateway.TokenValidadorGateway;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JwtTokenValidadorGateway implements TokenValidadorGateway {

	private final RSAPublicKey publicKey;

	JwtTokenValidadorGateway(RSAPublicKey jwtPublicKey) {
		this.publicKey = jwtPublicKey;
	}

	@Override
	public Optional<String> validarEExtrairIdentificador(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			if (!signedJWT.verify(new RSASSAVerifier(publicKey))) {
				return Optional.empty();
			}
			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
			Date expiracao = claims.getExpirationTime();
			if (expiracao == null || !expiracao.after(new Date())) {
				return Optional.empty();
			}
			return Optional.ofNullable(claims.getSubject());
		} catch (ParseException | JOSEException e) {
			return Optional.empty();
		}
	}
}
