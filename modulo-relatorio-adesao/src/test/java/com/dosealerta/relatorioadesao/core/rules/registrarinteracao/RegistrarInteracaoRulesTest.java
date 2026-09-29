package com.dosealerta.relatorioadesao.core.rules.registrarinteracao;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.INTERACAO_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.MEDICAMENTO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.umRegistroCom;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.umRegistroValido;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.relatorioadesao.TesteUnitarioBase;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.exception.AlarmeIdObrigatorioException;
import com.dosealerta.relatorioadesao.core.exception.InteracaoIdObrigatorioException;
import com.dosealerta.relatorioadesao.core.exception.MedicamentoObrigatorioException;
import com.dosealerta.relatorioadesao.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.relatorioadesao.core.exception.RegistradaEmObrigatoriaException;
import com.dosealerta.relatorioadesao.core.exception.TipoInteracaoObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrarInteracaoRulesTest extends TesteUnitarioBase {

	private static final TipoInteracao TIPO = TipoInteracao.CONFIRMACAO;

	private RegistrarInteracaoIdDeveSerInformadoRule idRule;
	private RegistrarInteracaoAlarmeIdDeveSerInformadoRule alarmeRule;
	private RegistrarInteracaoPacienteIdDeveSerInformadoRule pacienteRule;
	private RegistrarInteracaoMedicamentoDevePreenchidoRule medicamentoRule;
	private RegistrarInteracaoTipoDeveSerInformadoRule tipoRule;
	private RegistrarInteracaoRegistradaEmDeveSerInformadaRule registradaEmRule;

	@BeforeEach
	void setUp() {
		idRule = new RegistrarInteracaoIdDeveSerInformadoRule();
		alarmeRule = new RegistrarInteracaoAlarmeIdDeveSerInformadoRule();
		pacienteRule = new RegistrarInteracaoPacienteIdDeveSerInformadoRule();
		medicamentoRule = new RegistrarInteracaoMedicamentoDevePreenchidoRule();
		tipoRule = new RegistrarInteracaoTipoDeveSerInformadoRule();
		registradaEmRule = new RegistrarInteracaoRegistradaEmDeveSerInformadaRule();
	}

	private RegistroInteracaoContext contexto(RegistrarInteracaoInput input) {
		return new RegistroInteracaoContext(input);
	}

	@Test
	void devePassarQuandoTudoValido() {
		RegistroInteracaoContext contexto = contexto(umRegistroValido());

		assertDoesNotThrow(() -> idRule.validar(contexto));
		assertDoesNotThrow(() -> alarmeRule.validar(contexto));
		assertDoesNotThrow(() -> pacienteRule.validar(contexto));
		assertDoesNotThrow(() -> medicamentoRule.validar(contexto));
		assertDoesNotThrow(() -> tipoRule.validar(contexto));
		assertDoesNotThrow(() -> registradaEmRule.validar(contexto));
	}

	@Test
	void deveRejeitarIdNulo() {
		assertThrows(
				InteracaoIdObrigatorioException.class,
				() -> idRule.validar(contexto(umRegistroCom(null, PACIENTE_ID, MEDICAMENTO, TIPO, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarAlarmeIdNulo() {
		var input = new RegistrarInteracaoInput(INTERACAO_ID, null, PACIENTE_ID, MEDICAMENTO, TIPO, INSTANTE_FIXO);

		assertThrows(AlarmeIdObrigatorioException.class, () -> alarmeRule.validar(contexto(input)));
	}

	@Test
	void deveRejeitarPacienteIdNulo() {
		assertThrows(
				PacienteIdObrigatorioException.class,
				() -> pacienteRule.validar(contexto(umRegistroCom(INTERACAO_ID, null, MEDICAMENTO, TIPO, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarMedicamentoNuloOuEmBranco() {
		assertThrows(
				MedicamentoObrigatorioException.class,
				() -> medicamentoRule.validar(
						contexto(umRegistroCom(INTERACAO_ID, PACIENTE_ID, null, TIPO, INSTANTE_FIXO))));
		assertThrows(
				MedicamentoObrigatorioException.class,
				() -> medicamentoRule.validar(
						contexto(umRegistroCom(INTERACAO_ID, PACIENTE_ID, VALOR_EM_BRANCO, TIPO, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarTipoNulo() {
		assertThrows(
				TipoInteracaoObrigatorioException.class,
				() -> tipoRule.validar(
						contexto(umRegistroCom(INTERACAO_ID, PACIENTE_ID, MEDICAMENTO, null, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarDataDeRegistroNula() {
		assertThrows(
				RegistradaEmObrigatoriaException.class,
				() -> registradaEmRule.validar(
						contexto(umRegistroCom(INTERACAO_ID, PACIENTE_ID, MEDICAMENTO, TIPO, null))));
	}
}
