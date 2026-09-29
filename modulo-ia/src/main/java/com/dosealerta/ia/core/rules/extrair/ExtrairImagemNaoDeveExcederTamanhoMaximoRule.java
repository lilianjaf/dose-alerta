package com.dosealerta.ia.core.rules.extrair;

import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;

public class ExtrairImagemNaoDeveExcederTamanhoMaximoRule implements ValidadorExtracaoReceitaRule {

	private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;
	private static final String MENSAGEM_IMAGEM_GRANDE = "Imagem da receita excede o tamanho máximo de 10MB";

	@Override
	public void validar(ExtracaoReceitaContext context) {
		if (context.input().imagem().length > TAMANHO_MAXIMO_BYTES) {
			throw new ImagemReceitaInvalidaException(MENSAGEM_IMAGEM_GRANDE);
		}
	}
}
