package com.dosealerta.notificacao.infra.client;

record EnviarMensagemRequest(String telefone, String texto, String textoBotao) {
}
