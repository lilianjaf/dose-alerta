package com.dosealerta.relatorioadesao.core.gateway;

import java.util.Optional;

/**
 * Valida localmente o JWT emitido pelo modulo-usuario (defesa em profundidade — seção 2 do
 * RESUMO_TECNICO.md e Etapa 10.1) — este módulo nunca emite token, só valida.
 */
public interface TokenValidadorGateway {
	Optional<String> validarEExtrairIdentificador(String token);
}
