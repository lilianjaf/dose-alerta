package com.dosealerta.ia;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public final class IaFixtures {

	public static final Instant INSTANTE_FIXO = Instant.parse("2026-01-15T12:00:00Z");
	public static final Clock CLOCK_FIXO = Clock.fixed(INSTANTE_FIXO, ZoneId.of("America/Sao_Paulo"));
	public static final UUID PACIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID RECEITA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final String TELEFONE = "+5511999999999";
	public static final String MEDICAMENTO = "Losartana";
	public static final String DOSE = "50mg";
	public static final int FREQUENCIA_HORAS = 24;
	public static final int DURACAO_DIAS = 30;
	public static final String CORRELATION_ID = "correlacao-fixa";
	public static final String VALOR_EM_BRANCO = "  ";
	public static final byte[] IMAGEM = {1, 2, 3};

	private IaFixtures() {
	}

	public static Receita umaReceita() {
		return umaReceitaCom(MEDICAMENTO, DOSE, FREQUENCIA_HORAS, DURACAO_DIAS);
	}

	public static Receita umaReceitaCom(String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {
		return Receita.aguardandoConfirmacao(
				PACIENTE_ID, TELEFONE, medicamento, dose, frequenciaHoras, duracaoDias, INSTANTE_FIXO, INSTANTE_FIXO);
	}

	public static Receita umaReceitaConfirmada() {
		Receita receita = umaReceita();
		receita.confirmar(MEDICAMENTO, DOSE, FREQUENCIA_HORAS, DURACAO_DIAS, INSTANTE_FIXO, CORRELATION_ID);
		return receita;
	}

	public static ConfirmarReceitaInput umaConfirmacao(
			String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {
		return new ConfirmarReceitaInput(medicamento, dose, frequenciaHoras, duracaoDias);
	}

	public static ExtrairReceitaInput umaExtracaoValida() {
		return new ExtrairReceitaInput(PACIENTE_ID, TELEFONE, INSTANTE_FIXO, IMAGEM);
	}

	public static ExtrairReceitaInput umaExtracaoCom(
			UUID pacienteId, String telefone, Instant horarioInicial, byte[] imagem) {
		return new ExtrairReceitaInput(pacienteId, telefone, horarioInicial, imagem);
	}
}
