package com.dosealerta.notificacao.core.rules.solicitarenvio;

import static com.dosealerta.notificacao.NotificacaoFixtures.ALARME_ID;
import static com.dosealerta.notificacao.NotificacaoFixtures.DOSE;
import static com.dosealerta.notificacao.NotificacaoFixtures.MEDICAMENTO;
import static com.dosealerta.notificacao.NotificacaoFixtures.PACIENTE_ID;
import static com.dosealerta.notificacao.NotificacaoFixtures.TELEFONE;
import static com.dosealerta.notificacao.NotificacaoFixtures.umaSolicitacaoCom;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.notificacao.TesteUnitarioBase;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.exception.TelefoneFormatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolicitarEnvioFormatoRulesTest extends TesteUnitarioBase {

	private static final String TELEFONE_INVALIDO = "numero-invalido";
	private static final EtapaEscalonamento ETAPA = EtapaEscalonamento.LEMBRETE_INICIAL;

	private SolicitarEnvioTelefoneDeveTerFormatoValidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new SolicitarEnvioTelefoneDeveTerFormatoValidoRule();
	}

	@Test
	void devePassarQuandoTelefoneEstaEmFormatoValido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new SolicitacaoEnvioContext(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, ETAPA))));
	}

	@Test
	void deveRejeitarTelefoneForaDoFormato() {
		assertThrows(TelefoneFormatoInvalidoException.class, () -> telefoneRule.validar(new SolicitacaoEnvioContext(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, TELEFONE_INVALIDO, MEDICAMENTO, DOSE, ETAPA))));
	}

	@Test
	void deveIgnorarTelefoneNuloPoisOutraRegraCuidaDisso() {
		assertDoesNotThrow(() -> telefoneRule.validar(new SolicitacaoEnvioContext(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, null, MEDICAMENTO, DOSE, ETAPA))));
	}
}
