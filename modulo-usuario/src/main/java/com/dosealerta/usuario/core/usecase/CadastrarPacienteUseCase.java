package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;

public interface CadastrarPacienteUseCase {

	Paciente executar(CadastrarPacienteInput input);
}
