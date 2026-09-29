package com.dosealerta.usuario.core.rules.completarcadastro;

import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroComNumeroInscricao;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroComTelefone;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusFormatoInvalidoException;
import com.dosealerta.usuario.core.exception.TelefoneFormatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompletarCadastroFormatoRulesTest extends TesteUnitarioBase {

	private static final String TELEFONE_INVALIDO = "numero-invalido";

	private CompletarCadastroTelefoneDeveTerFormatoValidoRule telefoneRule;
	private CompletarCadastroNumeroInscricaoSusDeveTerQuinzeDigitosRule numeroRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new CompletarCadastroTelefoneDeveTerFormatoValidoRule();
		numeroRule = new CompletarCadastroNumeroInscricaoSusDeveTerQuinzeDigitosRule();
	}

	@Test
	void devePassarQuandoTelefoneEstaEmFormatoValido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new CompletarCadastroContext(umCompletarCadastroComTelefone(TELEFONE), null, null)));
	}

	@Test
	void deveRejeitarTelefoneForaDoFormato() {
		assertThrows(TelefoneFormatoInvalidoException.class, () -> telefoneRule.validar(new CompletarCadastroContext(umCompletarCadastroComTelefone(TELEFONE_INVALIDO), null, null)));
	}

	@Test
	void deveIgnorarTelefoneNuloPoisOutraRegraCuidaDisso() {
		assertDoesNotThrow(() -> telefoneRule.validar(new CompletarCadastroContext(umCompletarCadastroComTelefone(null), null, null)));
	}

	@Test
	void devePassarQuandoNumeroTemQuinzeDigitos() {
		assertDoesNotThrow(() -> numeroRule.validar(
				new CompletarCadastroContext(umCompletarCadastroComNumeroInscricao("700000000000001"), null, null)));
	}

	@Test
	void deveRejeitarNumeroForaDoFormato() {
		assertThrows(
				NumeroInscricaoSusFormatoInvalidoException.class,
				() -> numeroRule.validar(
						new CompletarCadastroContext(umCompletarCadastroComNumeroInscricao("123"), null, null)));
	}
}
