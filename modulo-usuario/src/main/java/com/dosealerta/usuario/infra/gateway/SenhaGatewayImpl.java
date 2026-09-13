package com.dosealerta.usuario.infra.gateway;

import com.dosealerta.usuario.core.gateway.SenhaGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class SenhaGatewayImpl implements SenhaGateway {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public String hash(String senhaPlana) {
		return encoder.encode(senhaPlana);
	}

	@Override
	public boolean confere(String senhaPlana, String hash) {
		return encoder.matches(senhaPlana, hash);
	}
}
