package com.dosealerta.usuario.core.gateway;

import java.util.Optional;

public interface CadastroSusGateway {

	Optional<String> buscarNomePorTelefone(String telefone);

	Optional<String> buscarNomePorNumeroInscricao(String numeroInscricaoSus);
}
