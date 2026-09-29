package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import java.util.UUID;

public interface BuscarReceitaUseCase {

	Receita executar(UUID id);
}
