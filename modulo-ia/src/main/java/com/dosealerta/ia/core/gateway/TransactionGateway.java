package com.dosealerta.ia.core.gateway;

import java.util.function.Supplier;

public interface TransactionGateway {

	<T> T execute(Supplier<T> acao);
}
