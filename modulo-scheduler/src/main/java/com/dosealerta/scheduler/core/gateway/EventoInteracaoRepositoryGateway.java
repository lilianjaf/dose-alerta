package com.dosealerta.scheduler.core.gateway;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventoInteracaoRepositoryGateway {

	List<EventoInteracao> buscarPendentes(int limite);

	void marcarComoPublicado(UUID id, Instant quando);
}
