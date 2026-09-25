package com.dosealerta.scheduler.core.gateway;

import java.util.Optional;

public interface TokenValidadorGateway {
	Optional<String> validarEExtrairIdentificador(String token);
}
