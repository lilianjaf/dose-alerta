package com.dosealerta.ia.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtrairReceitaUseCaseTest {

	@Mock
	private ExtratorReceitaGateway extratorReceitaGateway;

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	private ExtrairReceitaUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ExtrairReceitaUseCase(extratorReceitaGateway, receitaRepositoryGateway);
	}

	@Test
	void devePersistirAReceitaAguardandoConfirmacaoQuandoExtracaoEValida() {
		var input = new ExtrairReceitaInput(UUID.randomUUID(), "+5511999999999", Instant.now(), new byte[] {1, 2, 3});
		when(extratorReceitaGateway.extrair(input.imagem())).thenReturn(new ReceitaExtraida("Losartana", "50mg", 24, 30));
		when(receitaRepositoryGateway.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

		Receita resultado = useCase.executar(input);

		assertEquals("Losartana", resultado.getMedicamento());
		assertEquals(StatusReceita.AGUARDANDO_CONFIRMACAO, resultado.getStatus());
		verify(receitaRepositoryGateway).salvar(any(Receita.class));
	}

	@Test
	void naoDevePersistirQuandoGuardrailReprovaAExtracao() {
		var input = new ExtrairReceitaInput(UUID.randomUUID(), "+5511999999999", Instant.now(), new byte[] {1, 2, 3});
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(new ReceitaExtraida("Losartana", "dose-invalida", 24, 30));

		assertThrows(ReceitaInvalidaException.class, () -> useCase.executar(input));
		verifyNoInteractions(receitaRepositoryGateway);
	}
}
