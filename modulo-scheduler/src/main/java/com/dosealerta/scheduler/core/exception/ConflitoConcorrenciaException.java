package com.dosealerta.scheduler.core.exception;

import java.util.UUID;

/**
 * O alarme foi modificado por outro processo (job de escalonamento ou outra requisição de
 * confirmação) entre a leitura e a gravação desta operação — ver lock otimista em
 * {@code AlarmeJpaEntity}.
 */
public class ConflitoConcorrenciaException extends RuntimeException {

	public ConflitoConcorrenciaException(UUID alarmeId, Throwable causa) {
		super("Conflito de concorrência ao salvar o alarme " + alarmeId, causa);
	}
}
