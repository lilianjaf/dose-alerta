package com.dosealerta.relatorioadesao.core.gateway;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InteracaoRepositoryGateway {

	/**
	 * Idempotente: se já existir uma interação com este id (mesmo evento entregue mais de
	 * uma vez pelo publisher do modulo-scheduler — entrega é "pelo menos uma vez"), a
	 * chamada não tem efeito. Sem isso, uma reentrega duplicaria a contagem na taxa de
	 * adesão, que é um número em que o profissional de saúde baseia uma decisão clínica —
	 * diferente de duplicar um alarme ou uma mensagem, aqui o custo de um duplicado é maior.
	 */
	void salvar(Interacao interacao);

	List<Interacao> buscarPorPacienteEPeriodo(UUID pacienteId, Instant inicio, Instant fim);
}
