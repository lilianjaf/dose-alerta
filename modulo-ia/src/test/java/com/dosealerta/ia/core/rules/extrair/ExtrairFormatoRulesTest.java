package com.dosealerta.ia.core.rules.extrair;

import static com.dosealerta.ia.IaFixtures.IMAGEM;
import static com.dosealerta.ia.IaFixtures.INSTANTE_FIXO;
import static com.dosealerta.ia.IaFixtures.PACIENTE_ID;
import static com.dosealerta.ia.IaFixtures.TELEFONE;
import static com.dosealerta.ia.IaFixtures.umaExtracaoCom;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.exception.TelefoneFormatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExtrairFormatoRulesTest extends TesteUnitarioBase {

	private static final String TELEFONE_INVALIDO = "numero-invalido";

	private ExtrairTelefoneDeveTerFormatoValidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new ExtrairTelefoneDeveTerFormatoValidoRule();
	}

	@Test
	void devePassarQuandoTelefoneEstaEmFormatoValido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new ExtracaoReceitaContext(umaExtracaoCom(PACIENTE_ID, TELEFONE, INSTANTE_FIXO, IMAGEM))));
	}

	@Test
	void deveRejeitarTelefoneForaDoFormato() {
		assertThrows(TelefoneFormatoInvalidoException.class, () -> telefoneRule.validar(new ExtracaoReceitaContext(umaExtracaoCom(PACIENTE_ID, TELEFONE_INVALIDO, INSTANTE_FIXO, IMAGEM))));
	}

	@Test
	void deveIgnorarTelefoneNuloPoisOutraRegraCuidaDisso() {
		assertDoesNotThrow(() -> telefoneRule.validar(new ExtracaoReceitaContext(umaExtracaoCom(PACIENTE_ID, null, INSTANTE_FIXO, IMAGEM))));
	}
}
