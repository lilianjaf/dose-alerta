package com.dosealerta.usuario.infra.gateway;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.gateway.AutenticacaoGateway;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JwtAutenticacaoGateway implements AutenticacaoGateway {

	private static final Duration VALIDADE_TOKEN = Duration.ofHours(12);
	private static final String MENSAGEM_FALHA_EMITIR_TOKEN = "Falha ao emitir token JWT";

	private final RSAPrivateKey privateKey;
	private final RSAPublicKey publicKey;
	private final String issuer;
	private final Clock clock;

	JwtAutenticacaoGateway(
			RSAPrivateKey jwtPrivateKey,
			RSAPublicKey jwtPublicKey,
			@Value("${security.jwt.issuer}") String issuer,
			Clock clock) {
		this.privateKey = jwtPrivateKey;
		this.publicKey = jwtPublicKey;
		this.issuer = issuer;
		this.clock = clock;
	}

	@Override
	public String emitirToken(Paciente paciente) {
		try {
			Instant agora = clock.instant();
			JWTClaimsSet claims = new JWTClaimsSet.Builder()
					.subject(paciente.getId().toString())
					.issuer(issuer)
					.claim("telefone", paciente.getTelefone())
					.issueTime(Date.from(agora))
					.expirationTime(Date.from(agora.plus(VALIDADE_TOKEN)))
					.build();

			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
			signedJWT.sign(new RSASSASigner(privateKey));
			return signedJWT.serialize();
		} catch (Exception e) {
			throw new IllegalStateException(MENSAGEM_FALHA_EMITIR_TOKEN, e);
		}
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
			if (expiracao == null || !expiracao.after(Date.from(clock.instant()))) {
				return Optional.empty();
			}
			return Optional.ofNullable(claims.getSubject());
		} catch (ParseException | JOSEException e) {
			return Optional.empty();
		}
	}
}
