package com.dosealerta.scheduler;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class RelogioDeTeste extends Clock {

	private final ZoneId fusoHorario;
	private volatile Instant instante;

	public RelogioDeTeste(Instant instante, ZoneId fusoHorario) {
		this.instante = instante;
		this.fusoHorario = fusoHorario;
	}

	public void definir(Instant novoInstante) {
		this.instante = novoInstante;
	}

	@Override
	public ZoneId getZone() {
		return fusoHorario;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new RelogioDeTeste(instante, zone);
	}

	@Override
	public Instant instant() {
		return instante;
	}
}
