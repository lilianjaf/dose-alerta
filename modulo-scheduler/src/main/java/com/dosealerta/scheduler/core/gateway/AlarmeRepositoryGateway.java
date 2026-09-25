package com.dosealerta.scheduler.core.gateway;

import com.dosealerta.scheduler.core.domain.Alarme;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlarmeRepositoryGateway {

	Alarme salvar(Alarme alarme);

	Optional<Alarme> buscarPorId(UUID id);

	List<Alarme> buscarPendentesParaEscalonamento();

	Optional<Alarme> buscarPendenteMaisRecentePorTelefone(String telefone);
}
