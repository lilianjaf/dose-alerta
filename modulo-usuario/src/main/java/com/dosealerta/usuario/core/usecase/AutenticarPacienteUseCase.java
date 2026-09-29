package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.dto.TokenOutput;

public interface AutenticarPacienteUseCase {

	TokenOutput executar(AutenticarPacienteInput input);
}
