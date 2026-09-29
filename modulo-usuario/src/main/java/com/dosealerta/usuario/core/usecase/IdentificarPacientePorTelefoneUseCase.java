package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;

public interface IdentificarPacientePorTelefoneUseCase {

	IdentificarPacienteOutput executar(IdentificarPacienteInput input);
}
