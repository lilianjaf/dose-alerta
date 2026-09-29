package com.dosealerta.usuario.core.rules.identificacao;

import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.umaIdentificacaoComTelefone;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.exception.TelefoneFormatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentificacaoFormatoRulesTest extends TesteUnitarioBase {

	private static final String TELEFONE_INVALIDO = "numero-invalido";

	private IdentificacaoTelefoneDeveTerFormatoValidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new IdentificacaoTelefoneDeveTerFormatoValidoRule();
	}

	@Test
	void devePassarQuandoTelefoneEstaEmFormatoValido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new IdentificacaoPacienteContext(umaIdentificacaoComTelefone(TELEFONE))));
	}

	@Test
	void deveRejeitarTelefoneForaDoFormato() {
		assertThrows(TelefoneFormatoInvalidoException.class, () -> telefoneRule.validar(new IdentificacaoPacienteContext(umaIdentificacaoComTelefone(TELEFONE_INVALIDO))));
	}

	@Test
	void deveIgnorarTelefoneNuloPoisOutraRegraCuidaDisso() {
		assertDoesNotThrow(() -> telefoneRule.validar(new IdentificacaoPacienteContext(umaIdentificacaoComTelefone(null))));
	}
}
