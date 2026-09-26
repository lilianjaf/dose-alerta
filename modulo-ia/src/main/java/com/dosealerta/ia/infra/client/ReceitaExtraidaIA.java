package com.dosealerta.ia.infra.client;

record ReceitaExtraidaIA(
		boolean receitaMedica,
		String nomeMedico,
		String crm,
		String medicamento,
		String dose,
		Integer frequenciaHoras,
		Integer duracaoDias) {}
