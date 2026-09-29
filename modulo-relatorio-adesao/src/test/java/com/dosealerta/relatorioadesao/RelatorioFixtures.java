package com.dosealerta.relatorioadesao;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public final class RelatorioFixtures {

	public static final Instant INSTANTE_FIXO = Instant.parse("2026-01-15T12:00:00Z");
	public static final Clock CLOCK_FIXO = Clock.fixed(INSTANTE_FIXO, ZoneId.of("America/Sao_Paulo"));
	public static final UUID INTERACAO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final UUID ALARME_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	public static final UUID PACIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID OUTRO_PACIENTE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
	public static final String MEDICAMENTO = "Losartana";
	public static final String VALOR_EM_BRANCO = "  ";

	private RelatorioFixtures() {
	}

	public static UUID idDaInteracao(int sequencia) {
		return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(sequencia));
	}

	public static Interacao umaInteracao() {
		return umaInteracao(INTERACAO_ID, PACIENTE_ID, MEDICAMENTO, TipoInteracao.CONFIRMACAO);
	}

	public static Interacao umaInteracao(UUID id, UUID pacienteId, String medicamento, TipoInteracao tipo) {
		return Interacao.nova(id, pacienteId, medicamento, tipo, INSTANTE_FIXO);
	}

	public static RegistrarInteracaoInput umRegistroValido() {
		return umRegistroCom(INTERACAO_ID, PACIENTE_ID, MEDICAMENTO, TipoInteracao.CONFIRMACAO, INSTANTE_FIXO);
	}

	public static RegistrarInteracaoInput umRegistroCom(
			UUID id, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant registradaEm) {
		return new RegistrarInteracaoInput(id, ALARME_ID, pacienteId, medicamento, tipo, registradaEm);
	}
}
