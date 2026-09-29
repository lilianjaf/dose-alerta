package com.dosealerta.scheduler.core.dto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.contratos.Contrato;
import com.dosealerta.scheduler.TesteUnitarioBase;

import com.dosealerta.scheduler.core.rules.criar.CriacaoAlarmeContext;
import com.dosealerta.scheduler.core.rules.criar.CriarDoseDevePreenchidaRule;
import com.dosealerta.scheduler.core.rules.criar.CriarHorarioAlvoDeveSerInformadoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarMedicamentoDevePreenchidoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarPacienteIdDeveSerInformadoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarTelefoneDevePreenchidoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.scheduler.core.rules.criar.ValidadorCriacaoAlarmeRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistrarConfirmacaoTelefoneDevePreenchidoRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistrarConfirmacaoTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistroConfirmacaoContext;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.ValidadorRegistroConfirmacaoRule;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContratoConsumidorSchedulerTest extends TesteUnitarioBase {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	void criar_alarme_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("criar-alarme");
		CriarAlarmeInput input = MAPPER.readValue(contrato.exemplo(), CriarAlarmeInput.class);
		assertDoesNotThrow(() -> validar(input));
	}

	@Test
	void criar_alarme_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("criar-alarme");
		for (String campo : contrato.camposObrigatorios()) {
			CriarAlarmeInput input = MAPPER.readValue(contrato.semCampo(campo), CriarAlarmeInput.class);
			assertThrows(RuntimeException.class, () -> validar(input), "consumidor aceitou payload sem " + campo);
		}
	}

	@Test
	void resposta_paciente_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("resposta-paciente");
		RegistrarInteracaoPorTelefoneInput input = MAPPER.readValue(contrato.exemplo(), RegistrarInteracaoPorTelefoneInput.class);
		assertDoesNotThrow(() -> validar(input));
	}

	@Test
	void resposta_paciente_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("resposta-paciente");
		for (String campo : contrato.camposObrigatorios()) {
			RegistrarInteracaoPorTelefoneInput input = MAPPER.readValue(contrato.semCampo(campo), RegistrarInteracaoPorTelefoneInput.class);
			assertThrows(RuntimeException.class, () -> validar(input), "consumidor aceitou payload sem " + campo);
		}
	}

	private void validar(CriarAlarmeInput input) {
		List<ValidadorCriacaoAlarmeRule> regras = List.of(
				new CriarPacienteIdDeveSerInformadoRule(),
				new CriarTelefoneDevePreenchidoRule(),
				new CriarTelefoneDeveTerFormatoValidoRule(),
				new CriarMedicamentoDevePreenchidoRule(),
				new CriarDoseDevePreenchidaRule(),
				new CriarHorarioAlvoDeveSerInformadoRule());
		CriacaoAlarmeContext context = new CriacaoAlarmeContext(input);
		regras.forEach(regra -> regra.validar(context));
	}

	private void validar(RegistrarInteracaoPorTelefoneInput input) {
		List<ValidadorRegistroConfirmacaoRule> regras = List.of(
				new RegistrarConfirmacaoTelefoneDevePreenchidoRule(),
				new RegistrarConfirmacaoTelefoneDeveTerFormatoValidoRule());
		RegistroConfirmacaoContext context = new RegistroConfirmacaoContext(input.telefone(), null);
		regras.forEach(regra -> regra.validar(context));
	}
}
