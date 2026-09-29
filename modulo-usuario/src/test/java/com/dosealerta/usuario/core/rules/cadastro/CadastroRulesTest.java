package com.dosealerta.usuario.core.rules.cadastro;

import static com.dosealerta.usuario.UsuarioFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroComNome;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroComSenha;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroComTelefone;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroValido;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.exception.NomeObrigatorioException;
import com.dosealerta.usuario.core.exception.SenhaObrigatoriaException;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CadastroRulesTest extends TesteUnitarioBase {

	private CadastroNomeDevePreenchidoRule nomeRule;
	private CadastroTelefoneDevePreenchidoRule telefoneRule;
	private CadastroSenhaDevePreenchidaRule senhaRule;
	private CadastroTelefoneDeveSerUnicoRule telefoneUnicoRule;

	@BeforeEach
	void setUp() {
		nomeRule = new CadastroNomeDevePreenchidoRule();
		telefoneRule = new CadastroTelefoneDevePreenchidoRule();
		senhaRule = new CadastroSenhaDevePreenchidaRule();
		telefoneUnicoRule = new CadastroTelefoneDeveSerUnicoRule();
	}

	private CadastroPacienteContext contexto(CadastrarPacienteInput input) {
		return new CadastroPacienteContext(input, false);
	}

	@Test
	void devePassarQuandoTudoPreenchidoETelefoneInedito() {
		CadastroPacienteContext contexto = contexto(umCadastroValido());

		assertDoesNotThrow(() -> nomeRule.validar(contexto));
		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> senhaRule.validar(contexto));
		assertDoesNotThrow(() -> telefoneUnicoRule.validar(contexto));
	}

	@Test
	void deveRejeitarNomeNuloOuEmBranco() {
		assertThrows(NomeObrigatorioException.class, () -> nomeRule.validar(contexto(umCadastroComNome(null))));
		assertThrows(
				NomeObrigatorioException.class, () -> nomeRule.validar(contexto(umCadastroComNome(VALOR_EM_BRANCO))));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(contexto(umCadastroComTelefone(null))));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(contexto(umCadastroComTelefone(VALOR_EM_BRANCO))));
	}

	@Test
	void deveRejeitarSenhaNulaOuEmBranco() {
		assertThrows(SenhaObrigatoriaException.class, () -> senhaRule.validar(contexto(umCadastroComSenha(null))));
		assertThrows(
				SenhaObrigatoriaException.class,
				() -> senhaRule.validar(contexto(umCadastroComSenha(VALOR_EM_BRANCO))));
	}

	@Test
	void deveRejeitarTelefoneJaCadastrado() {
		CadastroPacienteContext contexto = new CadastroPacienteContext(umCadastroValido(), true);

		assertThrows(TelefoneJaCadastradoException.class, () -> telefoneUnicoRule.validar(contexto));
	}
}
