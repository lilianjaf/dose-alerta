package com.dosealerta.relatorioadesao.core.usecase;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.CLOCK_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.MEDICAMENTO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.idDaInteracao;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.umaInteracao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.relatorioadesao.TesteUnitarioBase;
import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import com.dosealerta.relatorioadesao.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.core.rules.consultartaxa.ConsultaTaxaAdesaoContext;
import com.dosealerta.relatorioadesao.core.rules.consultartaxa.ValidadorConsultaTaxaAdesaoRule;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class ConsultarTaxaAdesaoUseCaseImplTest extends TesteUnitarioBase {

	private static final Duration UMA_HORA = Duration.ofHours(1);

	@Mock
	private InteracaoRepositoryGateway interacaoRepositoryGateway;

	@Mock
	private ValidadorConsultaTaxaAdesaoRule regra;

	@Captor
	private ArgumentCaptor<ConsultaTaxaAdesaoContext> contextCaptor;

	private ConsultarTaxaAdesaoUseCaseImpl useCase;
	private Instant inicio;
	private List<Interacao> interacoes;

	@BeforeEach
	void setUp() {
		useCase = new ConsultarTaxaAdesaoUseCaseImpl(interacaoRepositoryGateway, CLOCK_FIXO, List.of(regra));
		inicio = INSTANTE_FIXO.minus(UMA_HORA);
		interacoes = List.of(
				umaInteracao(idDaInteracao(1), PACIENTE_ID, MEDICAMENTO, TipoInteracao.CONFIRMACAO),
				umaInteracao(idDaInteracao(2), PACIENTE_ID, MEDICAMENTO, TipoInteracao.NAO_CONFIRMACAO));
	}

	@Test
	void deveCalcularATaxaAPartirDasInteracoesDoPeriodo() {
		when(interacaoRepositoryGateway.buscarPorPacienteEPeriodo(PACIENTE_ID, inicio, INSTANTE_FIXO))
				.thenReturn(interacoes);

		List<TaxaAdesao> resultado = useCase.executar(PACIENTE_ID, inicio, INSTANTE_FIXO);

		assertEquals(1, resultado.size());
		assertEquals(MEDICAMENTO, resultado.get(0).medicamento());
		assertEquals(0.5, resultado.get(0).taxaConfirmacao());
	}

	@Test
	void deveUsarDesdeOInicioDosTemposEAteAgoraQuandoOPeriodoNaoEInformado() {
		when(interacaoRepositoryGateway.buscarPorPacienteEPeriodo(PACIENTE_ID, Instant.EPOCH, INSTANTE_FIXO))
				.thenReturn(interacoes);

		List<TaxaAdesao> resultado = useCase.executar(PACIENTE_ID, null, null);

		assertEquals(1, resultado.size());
		verify(regra).validar(contextCaptor.capture());
		assertEquals(Instant.EPOCH, contextCaptor.getValue().inicio());
		assertEquals(INSTANTE_FIXO, contextCaptor.getValue().fim());
	}

	@Test
	void naoDeveConsultarQuandoAlgumaRegraFalha() {
		doThrow(new PacienteIdObrigatorioException()).when(regra).validar(any());

		assertThrows(PacienteIdObrigatorioException.class, () -> useCase.executar(null, inicio, INSTANTE_FIXO));

		verifyNoInteractions(interacaoRepositoryGateway);
	}
}
