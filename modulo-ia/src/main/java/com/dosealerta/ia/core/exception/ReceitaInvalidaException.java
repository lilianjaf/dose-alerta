package com.dosealerta.ia.core.exception;

/**
 * A extração retornada pela IA não passou no guardrail (schema incompleto ou fora da faixa
 * plausível) — ver {@code RegraValidacaoReceitaExtraida}. Nesta etapa, a resposta ao paciente
 * é pedir para tentar novamente / preencher manualmente, já que o fallback semântico e o
 * reprompt automático (seção 6.5/6.6 do resumo técnico) são evoluções futuras (Etapa 7.10/7.11).
 */
public class ReceitaInvalidaException extends RuntimeException {

	public ReceitaInvalidaException(String motivo) {
		super("Extração da receita reprovada no guardrail: " + motivo);
	}
}
