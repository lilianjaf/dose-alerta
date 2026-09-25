package com.dosealerta.mensageria.core.gateway;

import java.util.Optional;

public interface TokenValidadorGateway {
	Optional<String> validarEExtrairIdentificador(String token);
}
