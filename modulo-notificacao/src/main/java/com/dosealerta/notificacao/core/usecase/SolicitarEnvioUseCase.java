package com.dosealerta.notificacao.core.usecase;

import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;

public interface SolicitarEnvioUseCase {

	void executar(SolicitarEnvioInput input);
}
