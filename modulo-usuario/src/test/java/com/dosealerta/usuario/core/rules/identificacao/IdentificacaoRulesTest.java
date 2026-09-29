package com.dosealerta.usuario.core.rules.identificacao;

import static com.dosealerta.usuario.UsuarioFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.usuario.UsuarioFixtures.umaIdentificacaoComTelefone;
import static com.dosealerta.usuario.UsuarioFixtures.umaIdentificacaoValida;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentificacaoRulesTest extends TesteUnitarioBase {

	private IdentificacaoTelefoneDevePreenchidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new IdentificacaoTelefoneDevePreenchidoRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new IdentificacaoPacienteContext(umaIdentificacaoValida())));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(new IdentificacaoPacienteContext(umaIdentificacaoComTelefone(null))));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(
						new IdentificacaoPacienteContext(umaIdentificacaoComTelefone(VALOR_EM_BRANCO))));
	}
}
