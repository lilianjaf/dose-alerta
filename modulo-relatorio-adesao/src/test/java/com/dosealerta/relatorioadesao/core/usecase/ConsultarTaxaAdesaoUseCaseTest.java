package com.dosealerta.relatorioadesao.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarTaxaAdesaoUseCaseTest {

	@Mock
	private InteracaoRepositoryGateway interacaoRepositoryGateway;

	private ConsultarTaxaAdesaoUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ConsultarTaxaAdesaoUseCase(interacaoRepositoryGateway);
	}

	@Test
	void deveCalcularATaxaAPartirDasInteracoesDoPeriodo() {
		UUID pacienteId = UUID.randomUUID();
		Instant inicio = Instant.now().minusSeconds(3600);
		Instant fim = Instant.now();
		when(interacaoRepositoryGateway.buscarPorPacienteEPeriodo(pacienteId, inicio, fim))
				.thenReturn(List.of(
						new Interacao(UUID.randomUUID(), pacienteId, "Losartana", TipoInteracao.CONFIRMACAO, fim),
						new Interacao(UUID.randomUUID(), pacienteId, "Losartana", TipoInteracao.NAO_CONFIRMACAO, fim)));

		List<TaxaAdesao> resultado = useCase.executar(pacienteId, inicio, fim);

		assertEquals(1, resultado.size());
		assertEquals("Losartana", resultado.get(0).medicamento());
		assertEquals(0.5, resultado.get(0).taxaConfirmacao());
	}
}
