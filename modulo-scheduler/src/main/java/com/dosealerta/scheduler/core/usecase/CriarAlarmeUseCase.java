package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.ResultadoCriarAlarme;

public interface CriarAlarmeUseCase {

	ResultadoCriarAlarme executar(CriarAlarmeInput input);
}
