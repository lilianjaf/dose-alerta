package com.dosealerta.relatorioadesao.core.dto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.contratos.Contrato;
import com.dosealerta.relatorioadesao.TesteUnitarioBase;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;

import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoAlarmeIdDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoIdDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoMedicamentoDevePreenchidoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoPacienteIdDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoRegistradaEmDeveSerInformadaRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoTipoDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistroInteracaoContext;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.ValidadorRegistroInteracaoRule;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContratoConsumidorRelatorioTest extends TesteUnitarioBase {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	void registrar_interacao_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("registrar-interacao");
		RegistrarInteracaoInput input = MAPPER.readValue(contrato.exemplo(), RegistrarInteracaoInput.class);
		assertDoesNotThrow(() -> validar(input));
	}

	@Test
	void registrar_interacao_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("registrar-interacao");
		for (String campo : contrato.camposObrigatorios()) {
			RegistrarInteracaoInput input = MAPPER.readValue(contrato.semCampo(campo), RegistrarInteracaoInput.class);
			assertThrows(RuntimeException.class, () -> validar(input), "consumidor aceitou payload sem " + campo);
		}
	}

	@Test
	void registrar_interacao_tipo_valoresDoEnumSaoOsMesmosDoContrato() {
		Set<String> doCodigo = new TreeSet<>();
		for (TipoInteracao valor : TipoInteracao.values()) {
			doCodigo.add(valor.name());
		}
		assertEquals(Contrato.carregar("registrar-interacao").valoresPermitidos("tipo"), doCodigo);
	}

	private void validar(RegistrarInteracaoInput input) {
		List<ValidadorRegistroInteracaoRule> regras = List.of(
				new RegistrarInteracaoIdDeveSerInformadoRule(),
				new RegistrarInteracaoAlarmeIdDeveSerInformadoRule(),
				new RegistrarInteracaoPacienteIdDeveSerInformadoRule(),
				new RegistrarInteracaoMedicamentoDevePreenchidoRule(),
				new RegistrarInteracaoTipoDeveSerInformadoRule(),
				new RegistrarInteracaoRegistradaEmDeveSerInformadaRule());
		RegistroInteracaoContext context = new RegistroInteracaoContext(input);
		regras.forEach(regra -> regra.validar(context));
	}
}
