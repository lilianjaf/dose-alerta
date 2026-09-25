package com.dosealerta.relatorioadesao.core.gateway;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InteracaoRepositoryGateway {

	void salvar(Interacao interacao);

	List<Interacao> buscarPorPacienteEPeriodo(UUID pacienteId, Instant inicio, Instant fim);
}
