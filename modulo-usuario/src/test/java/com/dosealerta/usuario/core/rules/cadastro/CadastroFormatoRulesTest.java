package com.dosealerta.usuario.core.rules.cadastro;

import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.SENHA;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroComSenha;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroComTelefone;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.exception.SenhaCurtaException;
import com.dosealerta.usuario.core.exception.TelefoneFormatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CadastroFormatoRulesTest extends TesteUnitarioBase {

	private static final String TELEFONE_INVALIDO = "numero-invalido";

	private CadastroTelefoneDeveTerFormatoValidoRule telefoneRule;
	private CadastroSenhaDeveTerTamanhoMinimoRule senhaRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new CadastroTelefoneDeveTerFormatoValidoRule();
		senhaRule = new CadastroSenhaDeveTerTamanhoMinimoRule();
	}

	@Test
	void devePassarQuandoTelefoneEstaEmFormatoValido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new CadastroPacienteContext(umCadastroComTelefone(TELEFONE), false)));
	}

	@Test
	void deveRejeitarTelefoneForaDoFormato() {
		assertThrows(TelefoneFormatoInvalidoException.class, () -> telefoneRule.validar(new CadastroPacienteContext(umCadastroComTelefone(TELEFONE_INVALIDO), false)));
	}

	@Test
	void deveIgnorarTelefoneNuloPoisOutraRegraCuidaDisso() {
		assertDoesNotThrow(() -> telefoneRule.validar(new CadastroPacienteContext(umCadastroComTelefone(null), false)));
	}

	@Test
	void devePassarQuandoSenhaTemOTamanhoMinimo() {
		assertDoesNotThrow(() -> senhaRule.validar(new CadastroPacienteContext(umCadastroComSenha(SENHA), false)));
	}

	@Test
	void deveRejeitarSenhaMaisCurtaQueOMinimo() {
		assertThrows(
				SenhaCurtaException.class,
				() -> senhaRule.validar(new CadastroPacienteContext(umCadastroComSenha("1234567"), false)));
	}
}
