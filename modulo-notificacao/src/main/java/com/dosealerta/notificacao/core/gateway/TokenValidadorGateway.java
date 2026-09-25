package com.dosealerta.notificacao.core.gateway;

import java.util.Optional;

public interface TokenValidadorGateway {
	Optional<String> validarEExtrairIdentificador(String token);
}
