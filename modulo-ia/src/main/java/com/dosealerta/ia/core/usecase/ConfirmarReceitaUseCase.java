package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import java.util.UUID;

public interface ConfirmarReceitaUseCase {

	Receita executar(UUID receitaId, ConfirmarReceitaInput input);
}
