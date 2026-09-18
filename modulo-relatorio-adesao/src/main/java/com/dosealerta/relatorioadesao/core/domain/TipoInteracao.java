package com.dosealerta.relatorioadesao.core.domain;

/**
 * Espelha {@code TipoInteracao} do modulo-scheduler — cada módulo mantém sua própria cópia
 * do vocabulário do evento que consome (ver seção 5 do resumo técnico: módulos não
 * compartilham classes de domínio entre si).
 */
public enum TipoInteracao {
	CONFIRMACAO,
	NAO_CONFIRMACAO,
	LIGACAO_ATENDIDA
}
