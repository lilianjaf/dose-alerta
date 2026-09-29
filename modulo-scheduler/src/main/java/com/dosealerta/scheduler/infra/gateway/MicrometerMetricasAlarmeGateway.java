package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
class MicrometerMetricasAlarmeGateway implements MetricasAlarmeGateway {

	private static final String RESULTADO_CONFIRMADO = "confirmado";
	private static final String RESULTADO_NAO_CONFIRMADO = "nao_confirmado";
	private static final String METRICA = "alarme.desfecho";
	private static final String TAG_RESULTADO = "resultado";

	private final MeterRegistry meterRegistry;

	MicrometerMetricasAlarmeGateway(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void registrarConfirmacao() {
		meterRegistry.counter(METRICA, TAG_RESULTADO, RESULTADO_CONFIRMADO).increment();
	}

	@Override
	public void registrarNaoConfirmacao() {
		meterRegistry.counter(METRICA, TAG_RESULTADO, RESULTADO_NAO_CONFIRMADO).increment();
	}
}
