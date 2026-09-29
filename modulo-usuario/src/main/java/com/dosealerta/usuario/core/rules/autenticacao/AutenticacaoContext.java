package com.dosealerta.usuario.core.rules.autenticacao;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;

public record AutenticacaoContext(AutenticarPacienteInput input, Paciente paciente, boolean senhaConfere) {
}
