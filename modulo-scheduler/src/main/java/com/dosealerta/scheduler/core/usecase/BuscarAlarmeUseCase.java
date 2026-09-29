package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import java.util.UUID;

public interface BuscarAlarmeUseCase {

	Alarme executar(UUID id);
}
