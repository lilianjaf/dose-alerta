package com.dosealerta.usuario.core.rules.completarcadastro;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;

public record CompletarCadastroContext(CompletarCadastroInput input, Paciente paciente, String nomeNoSus) {
}
