package com.dosealerta.usuario.core.domain;

import java.time.Instant;
import java.util.UUID;

public final class Paciente {

	private final UUID id;
	private final String nome;
	private final String telefone;
	private final String senhaHash;
	private final Instant criadoEm;

	private Paciente(UUID id, String nome, String telefone, String senhaHash, Instant criadoEm) {
		this.id = id;
		this.nome = nome;
		this.telefone = telefone;
		this.senhaHash = senhaHash;
		this.criadoEm = criadoEm;
	}

	public static Paciente novo(String nome, String telefone, String senhaHash, Instant criadoEm) {
		return new Paciente(UUID.randomUUID(), nome, telefone, senhaHash, criadoEm);
	}

	public static Paciente existente(UUID id, String nome, String telefone, String senhaHash, Instant criadoEm) {
		return new Paciente(id, nome, telefone, senhaHash, criadoEm);
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getTelefone() {
		return telefone;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public boolean cadastroIncompleto() {
		return nome == null;
	}
}
