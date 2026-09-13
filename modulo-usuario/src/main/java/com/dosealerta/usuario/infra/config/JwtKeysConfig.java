package com.dosealerta.usuario.infra.config;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtKeysConfig {

	@Bean
	public RSAPublicKey jwtPublicKey(@Value("${security.jwt.public-key}") String publicKeyBase64)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] bytes = Base64.getDecoder().decode(publicKeyBase64);
		var keySpec = new X509EncodedKeySpec(bytes);
		return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
	}

	@Bean
	public RSAPrivateKey jwtPrivateKey(@Value("${security.jwt.private-key}") String privateKeyBase64)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] bytes = Base64.getDecoder().decode(privateKeyBase64);
		var keySpec = new PKCS8EncodedKeySpec(bytes);
		return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
	}
}
