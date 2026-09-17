package com.dosealerta.scheduler.core.gateway;

import com.dosealerta.scheduler.core.domain.Alarme;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlarmeRepositoryGateway {

	Alarme salvar(Alarme alarme);

	Optional<Alarme> buscarPorId(UUID id);

	List<Alarme> buscarPendentesParaEscalonamento();

	/**
	 * Busca o alarme pendente mais recentemente enviado para o telefone informado — usado
	 * para correlacionar a resposta do paciente (botão do WhatsApp, dígito da ligação) ao
	 * alarme aguardando confirmação. Correlação simplificada por telefone, aceitável nesta
	 * etapa (o telefone pertence a um único paciente e, na prática, há no máximo um alarme
	 * em aberto por vez aguardando resposta).
	 */
	Optional<Alarme> buscarPendenteMaisRecentePorTelefone(String telefone);
}
