package com.dosealerta.ia.core.gateway;

import com.dosealerta.ia.core.domain.Receita;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceitaRepositoryGateway {

	Receita salvar(Receita receita);

	List<Receita> salvarTodas(List<Receita> receitas);

	Optional<Receita> buscarPorId(UUID id);

	Optional<Receita> buscarAguardandoConfirmacaoMaisRecentePorTelefone(String telefone);
}
