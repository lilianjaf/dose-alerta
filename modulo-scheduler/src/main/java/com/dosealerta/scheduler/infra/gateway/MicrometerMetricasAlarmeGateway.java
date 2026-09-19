package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
class MicrometerMetricasAlarmeGateway implements MetricasAlarmeGateway {

	private static final String METRICA = "alarme.desfecho";
	private static final String TAG_RESULTADO = "resultado";

	private final MeterRegistry meterRegistry;

	MicrometerMetricasAlarmeGateway(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void registrarConfirmacao() {
		meterRegistry.counter(METRICA, TAG_RESULTADO, "confirmado").increment();
	}

	@Override
	public void registrarNaoConfirmacao() {
		meterRegistry.counter(METRICA, TAG_RESULTADO, "nao_confirmado").increment();
	}
}
