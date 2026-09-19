package com.dosealerta.mensageria.infra.config;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Só a chave pública — este módulo apenas valida o JWT emitido pelo modulo-usuario. */
@Configuration
public class JwtKeysConfig {

	@Bean
	public RSAPublicKey jwtPublicKey(@Value("${security.jwt.public-key}") String publicKeyBase64)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] bytes = Base64.getDecoder().decode(publicKeyBase64);
		var keySpec = new X509EncodedKeySpec(bytes);
		return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
	}
}
