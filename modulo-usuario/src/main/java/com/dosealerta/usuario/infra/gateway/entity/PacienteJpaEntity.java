package com.dosealerta.usuario.infra.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paciente")
public class PacienteJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String telefone;

	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	protected PacienteJpaEntity() {
	}

	public PacienteJpaEntity(UUID id, String nome, String telefone, String senhaHash, Instant criadoEm) {
		this.id = id;
		this.nome = nome;
		this.telefone = telefone;
		this.senhaHash = senhaHash;
		this.criadoEm = criadoEm;
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
}
