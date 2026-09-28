package com.dosealerta.mensageria.infra.client;

record ConfirmarReceitaPorTelefoneRequest(
		String telefone, String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {
}
