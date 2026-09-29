package com.dosealerta.usuario.core.rules.cadastro;

import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;

public record CadastroPacienteContext(CadastrarPacienteInput input, boolean telefoneJaCadastrado) {
}
