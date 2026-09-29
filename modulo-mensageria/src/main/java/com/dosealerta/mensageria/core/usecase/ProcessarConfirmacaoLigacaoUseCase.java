package com.dosealerta.mensageria.core.usecase;

public interface ProcessarConfirmacaoLigacaoUseCase {

	boolean executar(String telefone, String digitos);
}
