package com.dosealerta.mensageria.core.gateway;

import com.dosealerta.mensageria.core.dto.IdentificarPacienteResultado;

public interface PacienteClientGateway {

	IdentificarPacienteResultado identificar(String telefone);

	void completarCadastro(String telefone, String numeroInscricaoSus);
}
