package com.dosealerta.ia.core.gateway;

import com.dosealerta.ia.core.domain.OutboxEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepositoryGateway {

	List<OutboxEvent> buscarPendentes(int limite);

	void marcarComoPublicado(UUID id, Instant quando);
}
