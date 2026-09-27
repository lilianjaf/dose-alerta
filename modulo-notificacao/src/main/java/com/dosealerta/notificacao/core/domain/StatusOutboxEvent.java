package com.dosealerta.notificacao.core.domain;

public enum StatusOutboxEvent {
	PENDENTE,
	PUBLICADO,
	/** Esgotou as tentativas de publicação sem sucesso. */
	FALHOU,
	/** Ficou velho demais para ainda fazer sentido enviar (o lembrete já foi superado pelas etapas seguintes). */
	EXPIRADO
}
