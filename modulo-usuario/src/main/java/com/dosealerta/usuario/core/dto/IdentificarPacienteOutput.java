package com.dosealerta.usuario.core.dto;

import com.dosealerta.usuario.core.domain.Paciente;
import java.util.UUID;

/**
 * {@code recemCriado} distingue o primeiro contato (acabou de criar o placeholder agora, ainda não perguntamos
 * nada) de um retorno a um cadastro incompleto já existente (já perguntamos o nome antes, e esta mensagem deve
 * ser tratada como a resposta) — os dois casos têm {@code cadastroCompleto=false}, mas exigem reações diferentes.
 */
public record IdentificarPacienteOutput(UUID pacienteId, String nome, boolean cadastroCompleto, boolean recemCriado) {

	public static IdentificarPacienteOutput deExistente(Paciente paciente) {
		return new IdentificarPacienteOutput(paciente.getId(), paciente.getNome(), !paciente.cadastroIncompleto(), false);
	}

	public static IdentificarPacienteOutput deRecemCriado(Paciente paciente) {
		return new IdentificarPacienteOutput(paciente.getId(), paciente.getNome(), !paciente.cadastroIncompleto(), true);
	}
}
