package com.dosealerta.scheduler.core.gateway;

import com.dosealerta.scheduler.core.domain.OutboxEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepositoryGateway {

	List<OutboxEvent> buscarPendentes(int limite);

	void marcarComoPublicado(UUID id, Instant quando);
}
