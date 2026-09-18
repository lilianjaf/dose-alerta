package com.dosealerta.relatorioadesao.core.usecase;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarInteracaoUseCaseTest {

	@Mock
	private InteracaoRepositoryGateway interacaoRepositoryGateway;

	private RegistrarInteracaoUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RegistrarInteracaoUseCase(interacaoRepositoryGateway);
	}

	@Test
	void deveSalvarAInteracaoRecebida() {
		UUID id = UUID.randomUUID();
		UUID pacienteId = UUID.randomUUID();
		Instant quando = Instant.now();
		var input = new RegistrarInteracaoInput(id, UUID.randomUUID(), pacienteId, "Losartana", TipoInteracao.CONFIRMACAO, quando);

		useCase.executar(input);

		verify(interacaoRepositoryGateway)
				.salvar(eq(new Interacao(id, pacienteId, "Losartana", TipoInteracao.CONFIRMACAO, quando)));
	}
}
