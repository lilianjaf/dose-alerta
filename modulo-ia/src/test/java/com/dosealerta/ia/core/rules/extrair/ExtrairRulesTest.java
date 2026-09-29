package com.dosealerta.ia.core.rules.extrair;

import static com.dosealerta.ia.IaFixtures.IMAGEM;
import static com.dosealerta.ia.IaFixtures.INSTANTE_FIXO;
import static com.dosealerta.ia.IaFixtures.PACIENTE_ID;
import static com.dosealerta.ia.IaFixtures.TELEFONE;
import static com.dosealerta.ia.IaFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.ia.IaFixtures.umaExtracaoCom;
import static com.dosealerta.ia.IaFixtures.umaExtracaoValida;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.exception.HorarioInicialObrigatorioException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.ia.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExtrairRulesTest extends TesteUnitarioBase {

	private static final int UM_BYTE_ACIMA_DO_LIMITE = 10 * 1024 * 1024 + 1;

	private ExtrairImagemDevePreenchidaRule imagemRule;
	private ExtrairImagemNaoDeveExcederTamanhoMaximoRule tamanhoRule;
	private ExtrairPacienteIdDeveSerInformadoRule pacienteRule;
	private ExtrairTelefoneDevePreenchidoRule telefoneRule;
	private ExtrairHorarioInicialDeveSerInformadoRule horarioRule;

	@BeforeEach
	void setUp() {
		imagemRule = new ExtrairImagemDevePreenchidaRule();
		tamanhoRule = new ExtrairImagemNaoDeveExcederTamanhoMaximoRule();
		pacienteRule = new ExtrairPacienteIdDeveSerInformadoRule();
		telefoneRule = new ExtrairTelefoneDevePreenchidoRule();
		horarioRule = new ExtrairHorarioInicialDeveSerInformadoRule();
	}

	private ExtracaoReceitaContext contexto(ExtrairReceitaInput input) {
		return new ExtracaoReceitaContext(input);
	}

	@Test
	void devePassarQuandoTudoValido() {
		ExtracaoReceitaContext contexto = contexto(umaExtracaoValida());

		assertDoesNotThrow(() -> imagemRule.validar(contexto));
		assertDoesNotThrow(() -> tamanhoRule.validar(contexto));
		assertDoesNotThrow(() -> pacienteRule.validar(contexto));
		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> horarioRule.validar(contexto));
	}

	@Test
	void deveRejeitarImagemNulaOuVazia() {
		assertThrows(
				ImagemReceitaInvalidaException.class,
				() -> imagemRule.validar(contexto(umaExtracaoCom(PACIENTE_ID, TELEFONE, INSTANTE_FIXO, null))));
		assertThrows(
				ImagemReceitaInvalidaException.class,
				() -> imagemRule.validar(contexto(umaExtracaoCom(PACIENTE_ID, TELEFONE, INSTANTE_FIXO, new byte[0]))));
	}

	@Test
	void deveRejeitarImagemAcimaDoLimite() {
		byte[] grande = new byte[UM_BYTE_ACIMA_DO_LIMITE];

		assertThrows(
				ImagemReceitaInvalidaException.class,
				() -> tamanhoRule.validar(contexto(umaExtracaoCom(PACIENTE_ID, TELEFONE, INSTANTE_FIXO, grande))));
	}

	@Test
	void deveRejeitarPacienteIdNulo() {
		assertThrows(
				PacienteIdObrigatorioException.class,
				() -> pacienteRule.validar(contexto(umaExtracaoCom(null, TELEFONE, INSTANTE_FIXO, IMAGEM))));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(contexto(umaExtracaoCom(PACIENTE_ID, null, INSTANTE_FIXO, IMAGEM))));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(
						contexto(umaExtracaoCom(PACIENTE_ID, VALOR_EM_BRANCO, INSTANTE_FIXO, IMAGEM))));
	}

	@Test
	void deveRejeitarHorarioInicialNulo() {
		assertThrows(
				HorarioInicialObrigatorioException.class,
				() -> horarioRule.validar(contexto(umaExtracaoCom(PACIENTE_ID, TELEFONE, null, IMAGEM))));
	}
}
