package com.dosealerta.usuario.core.rules.completarcadastro;

import static com.dosealerta.usuario.UsuarioFixtures.NOME_NO_SUS;
import static com.dosealerta.usuario.UsuarioFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroComNumeroInscricao;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroComTelefone;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroValido;
import static com.dosealerta.usuario.UsuarioFixtures.umPacienteIncompleto;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusObrigatorioException;
import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;
import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompletarCadastroRulesTest extends TesteUnitarioBase {

	private CompletarCadastroTelefoneDevePreenchidoRule telefoneRule;
	private CompletarCadastroNumeroInscricaoSusDevePreenchidoRule numeroRule;
	private CompletarCadastroPacienteDeveExistirRule pacienteRule;
	private CompletarCadastroNumeroInscricaoSusDeveExistirRule numeroExisteRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new CompletarCadastroTelefoneDevePreenchidoRule();
		numeroRule = new CompletarCadastroNumeroInscricaoSusDevePreenchidoRule();
		pacienteRule = new CompletarCadastroPacienteDeveExistirRule();
		numeroExisteRule = new CompletarCadastroNumeroInscricaoSusDeveExistirRule();
	}

	private CompletarCadastroContext contexto(CompletarCadastroInput input) {
		return new CompletarCadastroContext(input, umPacienteIncompleto(), NOME_NO_SUS);
	}

	@Test
	void devePassarQuandoTudoValido() {
		CompletarCadastroContext contexto = contexto(umCompletarCadastroValido());

		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> numeroRule.validar(contexto));
		assertDoesNotThrow(() -> pacienteRule.validar(contexto));
		assertDoesNotThrow(() -> numeroExisteRule.validar(contexto));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(contexto(umCompletarCadastroComTelefone(null))));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(contexto(umCompletarCadastroComTelefone(VALOR_EM_BRANCO))));
	}

	@Test
	void deveRejeitarNumeroDeInscricaoNuloOuEmBranco() {
		assertThrows(
				NumeroInscricaoSusObrigatorioException.class,
				() -> numeroRule.validar(contexto(umCompletarCadastroComNumeroInscricao(null))));
		assertThrows(
				NumeroInscricaoSusObrigatorioException.class,
				() -> numeroRule.validar(contexto(umCompletarCadastroComNumeroInscricao(VALOR_EM_BRANCO))));
	}

	@Test
	void deveRejeitarQuandoPacienteNaoExiste() {
		CompletarCadastroContext contexto =
				new CompletarCadastroContext(umCompletarCadastroValido(), null, NOME_NO_SUS);

		assertThrows(PacienteNaoEncontradoException.class, () -> pacienteRule.validar(contexto));
	}

	@Test
	void deveRejeitarQuandoNumeroDeInscricaoNaoExisteNoSus() {
		CompletarCadastroContext contexto =
				new CompletarCadastroContext(umCompletarCadastroValido(), umPacienteIncompleto(), null);

		assertThrows(NumeroInscricaoSusNaoEncontradoException.class, () -> numeroExisteRule.validar(contexto));
	}
}
