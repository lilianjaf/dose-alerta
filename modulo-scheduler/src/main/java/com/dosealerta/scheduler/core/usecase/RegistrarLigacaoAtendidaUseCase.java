package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;

public interface RegistrarLigacaoAtendidaUseCase {

	Alarme executar(String telefone);
}
