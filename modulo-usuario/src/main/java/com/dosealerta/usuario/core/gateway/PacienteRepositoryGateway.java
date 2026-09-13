package com.dosealerta.usuario.core.gateway;

import com.dosealerta.usuario.core.domain.Paciente;
import java.util.Optional;

public interface PacienteRepositoryGateway {

	Paciente salvar(Paciente paciente);

	Optional<Paciente> buscarPorTelefone(String telefone);
}
