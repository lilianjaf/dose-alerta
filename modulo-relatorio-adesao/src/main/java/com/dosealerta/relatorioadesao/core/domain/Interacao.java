package com.dosealerta.relatorioadesao.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Réplica local, no read model do modulo-relatorio-adesao, de uma interação do paciente
 * registrada no modulo-scheduler (ver {@code Alarme}/{@code Interacao} lá e o
 * {@code InteracaoRegistradaEvent} em CONTRATOS_EVENTOS.md). Não é o agregado raiz — é a
 * unidade mínima sobre a qual {@code RegraCalculoTaxaAdesao} computa a taxa de adesão.
 */
public record Interacao(UUID id, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant registradaEm) {

	public static Interacao nova(UUID id, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant quando) {
		return new Interacao(id, pacienteId, medicamento, tipo, quando);
	}
}
