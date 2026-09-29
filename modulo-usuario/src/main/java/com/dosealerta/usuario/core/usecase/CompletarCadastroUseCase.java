package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;

public interface CompletarCadastroUseCase {

	Paciente executar(CompletarCadastroInput input);
}
