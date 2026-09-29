package com.dosealerta.notificacao.core.gateway;

import com.dosealerta.notificacao.core.domain.OutboxEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepositoryGateway {

	OutboxEvent salvar(OutboxEvent evento);

	List<OutboxEvent> buscarPendentes(int limite, Instant agora);

	void marcarComoPublicado(UUID id, Instant quando);

	void registrarFalha(UUID id, int tentativas, Instant proximaTentativaEm);

	void marcarComoFalhou(UUID id, int tentativas);

	void marcarComoExpirado(UUID id);
}
