package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;

public interface ConfirmarReceitaPorTelefoneUseCase {

	Receita executar(String telefone, ConfirmarReceitaInput correcoes);
}
