package com.dosealerta.usuario.core.rules.autenticacao;

import static com.dosealerta.usuario.UsuarioFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.usuario.UsuarioFixtures.umLoginComSenha;
import static com.dosealerta.usuario.UsuarioFixtures.umLoginComTelefone;
import static com.dosealerta.usuario.UsuarioFixtures.umLoginValido;
import static com.dosealerta.usuario.UsuarioFixtures.umPaciente;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.exception.SenhaObrigatoriaException;
import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutenticacaoRulesTest extends TesteUnitarioBase {

	private AutenticacaoTelefoneDevePreenchidoRule telefoneRule;
	private AutenticacaoSenhaDevePreenchidaRule senhaRule;
	private AutenticacaoPacienteDeveExistirRule pacienteRule;
	private AutenticacaoSenhaDeveConferirRule conferirRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new AutenticacaoTelefoneDevePreenchidoRule();
		senhaRule = new AutenticacaoSenhaDevePreenchidaRule();
		pacienteRule = new AutenticacaoPacienteDeveExistirRule();
		conferirRule = new AutenticacaoSenhaDeveConferirRule();
	}

	private AutenticacaoContext contexto(AutenticarPacienteInput input) {
		return new AutenticacaoContext(input, umPaciente(), true);
	}

	@Test
	void devePassarQuandoTudoValido() {
		AutenticacaoContext contexto = contexto(umLoginValido());

		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> senhaRule.validar(contexto));
		assertDoesNotThrow(() -> pacienteRule.validar(contexto));
		assertDoesNotThrow(() -> conferirRule.validar(contexto));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(contexto(umLoginComTelefone(null))));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(contexto(umLoginComTelefone(VALOR_EM_BRANCO))));
	}

	@Test
	void deveRejeitarSenhaNulaOuEmBranco() {
		assertThrows(SenhaObrigatoriaException.class, () -> senhaRule.validar(contexto(umLoginComSenha(null))));
		assertThrows(
				SenhaObrigatoriaException.class, () -> senhaRule.validar(contexto(umLoginComSenha(VALOR_EM_BRANCO))));
	}

	@Test
	void deveRejeitarQuandoPacienteNaoExiste() {
		AutenticacaoContext contexto = new AutenticacaoContext(umLoginValido(), null, false);

		assertThrows(CredenciaisInvalidasException.class, () -> pacienteRule.validar(contexto));
	}

	@Test
	void deveRejeitarQuandoSenhaNaoConfere() {
		AutenticacaoContext contexto = new AutenticacaoContext(umLoginValido(), umPaciente(), false);

		assertThrows(CredenciaisInvalidasException.class, () -> conferirRule.validar(contexto));
	}
}
