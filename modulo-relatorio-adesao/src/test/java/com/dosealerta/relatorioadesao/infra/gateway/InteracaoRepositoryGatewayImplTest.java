package com.dosealerta.relatorioadesao.infra.gateway;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.MEDICAMENTO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.OUTRO_PACIENTE_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.idDaInteracao;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.umaInteracao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.relatorioadesao.TesteIntegracaoBase;
import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InteracaoRepositoryGatewayImplTest extends TesteIntegracaoBase {

	@Autowired
	private InteracaoRepositoryGateway interacaoRepositoryGateway;

	@Test
	void deveSalvarEBuscarInteracoesDoPacienteNoPeriodo() {
		interacaoRepositoryGateway.salvar(umaInteracao(idDaInteracao(1), PACIENTE_ID, MEDICAMENTO, TipoInteracao.CONFIRMACAO));
		interacaoRepositoryGateway.salvar(
				umaInteracao(idDaInteracao(2), OUTRO_PACIENTE_ID, MEDICAMENTO, TipoInteracao.CONFIRMACAO));

		List<Interacao> encontradas = interacaoRepositoryGateway.buscarPorPacienteEPeriodo(
				PACIENTE_ID, INSTANTE_FIXO.minusSeconds(60), INSTANTE_FIXO.plusSeconds(60));

		assertEquals(1, encontradas.size());
		assertTrue(encontradas.stream().allMatch(i -> i.pacienteId().equals(PACIENTE_ID)));
	}

	@Test
	void deveSerIdempotenteAoSalvarAMesmaInteracaoDuasVezes() {
		Interacao interacao = umaInteracao();

		interacaoRepositoryGateway.salvar(interacao);
		interacaoRepositoryGateway.salvar(interacao);

		List<Interacao> encontradas = interacaoRepositoryGateway.buscarPorPacienteEPeriodo(
				PACIENTE_ID, INSTANTE_FIXO.minusSeconds(60), INSTANTE_FIXO.plusSeconds(60));
		assertEquals(1, encontradas.size());
	}
}
