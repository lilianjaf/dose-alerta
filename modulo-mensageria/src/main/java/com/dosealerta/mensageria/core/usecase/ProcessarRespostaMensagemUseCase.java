package com.dosealerta.mensageria.core.usecase;

public interface ProcessarRespostaMensagemUseCase {

	boolean executar(String telefone, String corpo, String textoBotao);
}
