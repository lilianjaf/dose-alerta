package com.dosealerta.ia.core.rules.extrair;

import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;

public class ExtrairImagemDevePreenchidaRule implements ValidadorExtracaoReceitaRule {

	private static final String MENSAGEM_IMAGEM_VAZIA = "Imagem da receita não pode ser vazia";

	@Override
	public void validar(ExtracaoReceitaContext context) {
		byte[] imagem = context.input().imagem();
		if (imagem == null || imagem.length == 0) {
			throw new ImagemReceitaInvalidaException(MENSAGEM_IMAGEM_VAZIA);
		}
	}
}
