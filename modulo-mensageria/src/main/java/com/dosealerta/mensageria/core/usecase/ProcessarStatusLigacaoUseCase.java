package com.dosealerta.mensageria.core.usecase;

public interface ProcessarStatusLigacaoUseCase {

	void executar(String telefone, String callStatus);
}
