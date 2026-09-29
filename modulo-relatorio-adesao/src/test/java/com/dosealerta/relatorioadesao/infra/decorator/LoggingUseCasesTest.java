package com.dosealerta.relatorioadesao.infra.decorator;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.umRegistroValido;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.relatorioadesao.TesteUnitarioBase;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.relatorioadesao.core.usecase.ConsultarTaxaAdesaoUseCase;
import com.dosealerta.relatorioadesao.core.usecase.RegistrarInteracaoUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class LoggingUseCasesTest extends TesteUnitarioBase {

	@Mock
	private RegistrarInteracaoUseCase registrar;

	@Mock
	private ConsultarTaxaAdesaoUseCase consultar;

	private RegistrarInteracaoInput input;

	@BeforeEach
	void setUp() {
		input = umRegistroValido();
	}

	@Test
	void deveDelegarORegistro() {
		new LoggingRegistrarInteracaoUseCase(registrar).executar(input);

		verify(registrar).executar(input);
	}

	@Test
	void deveDelegarAConsultaEDevolverOResultado() {
		List<TaxaAdesao> taxas = List.of();
		when(consultar.executar(PACIENTE_ID, INSTANTE_FIXO, INSTANTE_FIXO)).thenReturn(taxas);

		assertSame(
				taxas,
				new LoggingConsultarTaxaAdesaoUseCase(consultar).executar(PACIENTE_ID, INSTANTE_FIXO, INSTANTE_FIXO));
	}

	@Test
	void devePropagarAExcecaoDoDelegate() {
		doThrow(new PacienteIdObrigatorioException()).when(registrar).executar(input);

		assertThrows(
				PacienteIdObrigatorioException.class, () -> new LoggingRegistrarInteracaoUseCase(registrar).executar(input));
	}
}
