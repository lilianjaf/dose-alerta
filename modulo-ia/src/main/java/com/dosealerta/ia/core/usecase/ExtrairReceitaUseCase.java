package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.ResultadoExtracao;

public interface ExtrairReceitaUseCase {

	ResultadoExtracao executar(ExtrairReceitaInput input);
}
