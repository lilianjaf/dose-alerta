package com.dosealerta.notificacao.core.gateway;

import com.dosealerta.notificacao.core.domain.OutboxEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepositoryGateway {

	OutboxEvent salvar(OutboxEvent evento);

	/** Eventos pendentes cuja próxima tentativa já venceu (ou que ainda não foram tentados). */
	List<OutboxEvent> buscarPendentes(int limite, Instant agora);

	void marcarComoPublicado(UUID id, Instant quando);

	/** Registra uma falha de publicação e agenda a próxima tentativa. */
	void registrarFalha(UUID id, int tentativas, Instant proximaTentativaEm);

	/** Esgotou as tentativas: não será mais retentado. */
	void marcarComoFalhou(UUID id, int tentativas);

	/** Velho demais para ser enviado: não será mais retentado. */
	void marcarComoExpirado(UUID id);
}
