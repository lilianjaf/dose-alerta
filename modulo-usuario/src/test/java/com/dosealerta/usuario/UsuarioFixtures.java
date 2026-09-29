package com.dosealerta.usuario;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public final class UsuarioFixtures {

	public static final Instant INSTANTE_FIXO = Instant.parse("2026-01-15T12:00:00Z");
	public static final Clock CLOCK_FIXO = Clock.fixed(INSTANTE_FIXO, ZoneId.of("America/Sao_Paulo"));
	public static final String NOME = "Maria da Silva";
	public static final String NOME_NO_SUS = "Pedro Alves";
	public static final String TELEFONE = "+5511999999999";
	public static final String SENHA = "senha1234";
	public static final String SENHA_ERRADA = "senha-errada";
	public static final String SENHA_HASH = "hash-fake";
	public static final String NUMERO_INSCRICAO_SUS = "700000000000001";
	public static final String VALOR_EM_BRANCO = "  ";

	private UsuarioFixtures() {
	}

	public static Paciente umPaciente() {
		return Paciente.novo(NOME, TELEFONE, SENHA_HASH, INSTANTE_FIXO);
	}

	public static Paciente umPacienteIncompleto() {
		return Paciente.novo(null, TELEFONE, null, INSTANTE_FIXO);
	}

	public static CadastrarPacienteInput umCadastroValido() {
		return new CadastrarPacienteInput(NOME, TELEFONE, SENHA);
	}

	public static CadastrarPacienteInput umCadastroComNome(String nome) {
		return new CadastrarPacienteInput(nome, TELEFONE, SENHA);
	}

	public static CadastrarPacienteInput umCadastroComTelefone(String telefone) {
		return new CadastrarPacienteInput(NOME, telefone, SENHA);
	}

	public static CadastrarPacienteInput umCadastroComSenha(String senha) {
		return new CadastrarPacienteInput(NOME, TELEFONE, senha);
	}

	public static AutenticarPacienteInput umLoginValido() {
		return new AutenticarPacienteInput(TELEFONE, SENHA);
	}

	public static AutenticarPacienteInput umLoginComSenha(String senha) {
		return new AutenticarPacienteInput(TELEFONE, senha);
	}

	public static AutenticarPacienteInput umLoginComTelefone(String telefone) {
		return new AutenticarPacienteInput(telefone, SENHA);
	}

	public static IdentificarPacienteInput umaIdentificacaoValida() {
		return new IdentificarPacienteInput(TELEFONE);
	}

	public static IdentificarPacienteInput umaIdentificacaoComTelefone(String telefone) {
		return new IdentificarPacienteInput(telefone);
	}

	public static CompletarCadastroInput umCompletarCadastroValido() {
		return new CompletarCadastroInput(TELEFONE, NUMERO_INSCRICAO_SUS);
	}

	public static CompletarCadastroInput umCompletarCadastroComTelefone(String telefone) {
		return new CompletarCadastroInput(telefone, NUMERO_INSCRICAO_SUS);
	}

	public static CompletarCadastroInput umCompletarCadastroComNumeroInscricao(String numeroInscricaoSus) {
		return new CompletarCadastroInput(TELEFONE, numeroInscricaoSus);
	}
}
