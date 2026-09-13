package com.dosealerta.usuario.core.gateway;

import com.dosealerta.usuario.core.domain.Paciente;
import java.util.Optional;

public interface AutenticacaoGateway {

	String emitirToken(Paciente paciente);

	Optional<String> validarEExtrairIdentificador(String token);
}
