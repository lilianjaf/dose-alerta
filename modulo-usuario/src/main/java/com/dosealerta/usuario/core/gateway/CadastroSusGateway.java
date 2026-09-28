package com.dosealerta.usuario.core.gateway;

import java.util.Optional;

/** Consulta o cadastro do paciente no SUS (simulado no hackathon pelo `modulo-sus-mock`). */
public interface CadastroSusGateway {

	Optional<String> buscarNomePorTelefone(String telefone);

	Optional<String> buscarNomePorNumeroInscricao(String numeroInscricaoSus);
}
