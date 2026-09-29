package com.dosealerta.notificacao.core.dto;

import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import java.util.UUID;

public record SolicitarEnvioInput(
		UUID alarmeId,
		UUID pacienteId,
		String telefone,
		String medicamento,
		String dose,
		EtapaEscalonamento etapa) {
}
