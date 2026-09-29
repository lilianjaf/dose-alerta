package com.dosealerta.relatorioadesao.core.usecase;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.umaInteracao;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.umRegistroValido;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.dosealerta.relatorioadesao.TesteUnitarioBase;
import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.exception.MedicamentoObrigatorioException;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.ValidadorRegistroInteracaoRule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class RegistrarInteracaoUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private InteracaoRepositoryGateway interacaoRepositoryGateway;

	@Mock
	private ValidadorRegistroInteracaoRule regra;

	private RegistrarInteracaoUseCaseImpl useCase;
	private RegistrarInteracaoInput input;

	@BeforeEach
	void setUp() {
		useCase = new RegistrarInteracaoUseCaseImpl(interacaoRepositoryGateway, List.of(regra));
		input = umRegistroValido();
	}

	@Test
	void deveSalvarAInteracaoRecebida() {
		useCase.executar(input);

		verify(interacaoRepositoryGateway).salvar(umaInteracao());
	}

	@Test
	void naoDeveSalvarQuandoAlgumaRegraFalha() {
		doThrow(new MedicamentoObrigatorioException()).when(regra).validar(any());

		assertThrows(MedicamentoObrigatorioException.class, () -> useCase.executar(input));

		verifyNoInteractions(interacaoRepositoryGateway);
	}
}
