package com.dosealerta.mensageria.core.dto;

public record DadosMensagemRecebida(
		String telefone, String corpo, String textoBotao, String mediaUrl0, int numMedia, String mediaContentType0) {

	public boolean temFoto() {
		return numMedia > 0 && mediaUrl0 != null;
	}

	public boolean temTexto() {
		return corpoOuBotao() != null;
	}

	public String corpoOuBotao() {
		if (textoBotao != null && !textoBotao.isBlank()) {
			return textoBotao;
		}
		return corpo != null && !corpo.isBlank() ? corpo : null;
	}
}
