package com.dosealerta.relatorioadesao.core.rules.consultartaxa;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.relatorioadesao.TesteUnitarioBase;
import com.dosealerta.relatorioadesao.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.relatorioadesao.core.exception.PeriodoInvalidoException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultarTaxaRulesTest extends TesteUnitarioBase {

	private static final Duration UMA_HORA = Duration.ofHours(1);

	private ConsultarTaxaPacienteIdDeveSerInformadoRule pacienteRule;
	private ConsultarTaxaPeriodoDeveSerValidoRule periodoRule;

	@BeforeEach
	void setUp() {
		pacienteRule = new ConsultarTaxaPacienteIdDeveSerInformadoRule();
		periodoRule = new ConsultarTaxaPeriodoDeveSerValidoRule();
	}

	@Test
	void devePassarQuandoPacienteInformadoEPeriodoValido() {
		ConsultaTaxaAdesaoContext contexto =
				new ConsultaTaxaAdesaoContext(PACIENTE_ID, INSTANTE_FIXO.minus(UMA_HORA), INSTANTE_FIXO);

		assertDoesNotThrow(() -> pacienteRule.validar(contexto));
		assertDoesNotThrow(() -> periodoRule.validar(contexto));
	}

	@Test
	void devePermitirInicioIgualAoFim() {
		assertDoesNotThrow(() -> periodoRule.validar(
				new ConsultaTaxaAdesaoContext(PACIENTE_ID, INSTANTE_FIXO, INSTANTE_FIXO)));
	}

	@Test
	void deveRejeitarPacienteIdNulo() {
		assertThrows(
				PacienteIdObrigatorioException.class,
				() -> pacienteRule.validar(new ConsultaTaxaAdesaoContext(null, INSTANTE_FIXO, INSTANTE_FIXO)));
	}

	@Test
	void deveRejeitarInicioPosteriorAoFim() {
		assertThrows(
				PeriodoInvalidoException.class,
				() -> periodoRule.validar(
						new ConsultaTaxaAdesaoContext(PACIENTE_ID, INSTANTE_FIXO, INSTANTE_FIXO.minus(UMA_HORA))));
	}
}
