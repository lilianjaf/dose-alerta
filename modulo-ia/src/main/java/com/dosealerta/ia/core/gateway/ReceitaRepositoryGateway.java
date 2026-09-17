package com.dosealerta.ia.core.gateway;

import com.dosealerta.ia.core.domain.Receita;
import java.util.Optional;
import java.util.UUID;

public interface ReceitaRepositoryGateway {

	Receita salvar(Receita receita);

	Optional<Receita> buscarPorId(UUID id);
}
