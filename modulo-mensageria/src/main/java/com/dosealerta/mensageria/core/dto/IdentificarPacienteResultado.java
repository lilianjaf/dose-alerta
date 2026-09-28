package com.dosealerta.mensageria.core.dto;

import java.util.UUID;

/**
 * {@code recemCriado} distingue o primeiro contato desse telefone (ainda não perguntamos nada) de um retorno a
 * um cadastro incompleto já existente (já perguntamos o nome, e esta mensagem é a resposta).
 */
public record IdentificarPacienteResultado(UUID pacienteId, String nome, boolean cadastroCompleto, boolean recemCriado) {
}
