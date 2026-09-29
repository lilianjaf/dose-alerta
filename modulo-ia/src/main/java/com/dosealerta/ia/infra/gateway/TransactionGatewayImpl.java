package com.dosealerta.ia.infra.gateway;

import com.dosealerta.ia.core.gateway.TransactionGateway;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class TransactionGatewayImpl implements TransactionGateway {

	private final TransactionTemplate transactionTemplate;

	TransactionGatewayImpl(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public <T> T execute(Supplier<T> acao) {
		return transactionTemplate.execute(status -> acao.get());
	}
}
