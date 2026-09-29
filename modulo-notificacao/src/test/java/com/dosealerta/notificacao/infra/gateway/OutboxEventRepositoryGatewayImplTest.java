package com.dosealerta.notificacao.infra.gateway;

import static com.dosealerta.notificacao.NotificacaoFixtures.INSTANTE_FIXO;
import static com.dosealerta.notificacao.NotificacaoFixtures.OUTRO_ALARME_ID;
import static com.dosealerta.notificacao.NotificacaoFixtures.umEventoOutbox;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.notificacao.TesteIntegracaoBase;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OutboxEventRepositoryGatewayImplTest extends TesteIntegracaoBase {

	@Autowired
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Test
	void deveSalvarEListarEventoPendente() {
		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(umEventoOutbox());

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10, INSTANTE_FIXO);

		assertTrue(pendentes.stream().anyMatch(e -> e.id().equals(salvo.id())));
	}

	@Test
	void deveMarcarEventoComoPublicadoERemoverDaListaDePendentes() {
		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(umEventoOutbox());

		outboxEventRepositoryGateway.marcarComoPublicado(salvo.id(), INSTANTE_FIXO);

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10, INSTANTE_FIXO);
		assertTrue(pendentes.stream().noneMatch(e -> e.id().equals(salvo.id())));
	}

	@Test
	void deveAdiarOEventoAteAProximaTentativaERetomarQuandoVencer() {
		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(umEventoOutbox());
		Instant agora = INSTANTE_FIXO;

		outboxEventRepositoryGateway.registrarFalha(salvo.id(), 1, agora.plusSeconds(60));

		assertTrue(outboxEventRepositoryGateway.buscarPendentes(50, agora).stream()
				.noneMatch(e -> e.id().equals(salvo.id())));
		var vencido = outboxEventRepositoryGateway.buscarPendentes(50, agora.plusSeconds(61)).stream()
				.filter(e -> e.id().equals(salvo.id()))
				.findFirst()
				.orElseThrow();
		assertEquals(1, vencido.tentativas());
	}

	@Test
	void deveTirarDaFilaOEventoQueFalhouOuExpirou() {
		OutboxEvent falhou = outboxEventRepositoryGateway.salvar(umEventoOutbox());
		OutboxEvent expirado = outboxEventRepositoryGateway.salvar(umEventoOutbox(OUTRO_ALARME_ID));

		outboxEventRepositoryGateway.marcarComoFalhou(falhou.id(), 5);
		outboxEventRepositoryGateway.marcarComoExpirado(expirado.id());

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(50, INSTANTE_FIXO.plusSeconds(3600));
		assertTrue(pendentes.stream().noneMatch(e -> e.id().equals(falhou.id()) || e.id().equals(expirado.id())));
	}

	@Test
	void deveManterCamposAoSalvar() {
		OutboxEvent original = umEventoOutbox();

		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(original);

		assertEquals(original.alarmeId(), salvo.alarmeId());
		assertEquals(original.pacienteId(), salvo.pacienteId());
		assertEquals(original.telefone(), salvo.telefone());
		assertEquals(original.medicamento(), salvo.medicamento());
		assertEquals(original.dose(), salvo.dose());
		assertEquals(original.etapa(), salvo.etapa());
		assertEquals(original.canal(), salvo.canal());
		assertEquals(StatusOutboxEvent.PENDENTE, salvo.status());
	}
}
