CREATE TABLE receita (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    medicamento VARCHAR(255) NOT NULL,
    dose VARCHAR(100) NOT NULL,
    frequencia_horas INTEGER NOT NULL,
    duracao_dias INTEGER NOT NULL,
    horario_inicial TIMESTAMP NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL
);

CREATE INDEX idx_receita_status ON receita (status);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    receita_id UUID NOT NULL REFERENCES receita (id),
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    publicado_em TIMESTAMP
);

CREATE INDEX idx_outbox_event_status ON outbox_event (status);

CREATE TABLE feedback_extracao (
    id UUID PRIMARY KEY,
    receita_id UUID NOT NULL REFERENCES receita (id),
    medicamento_extraido VARCHAR(255) NOT NULL,
    dose_extraida VARCHAR(100) NOT NULL,
    frequencia_extraida_horas INTEGER NOT NULL,
    duracao_extraida_dias INTEGER NOT NULL,
    medicamento_confirmado VARCHAR(255) NOT NULL,
    dose_confirmada VARCHAR(100) NOT NULL,
    frequencia_confirmada_horas INTEGER NOT NULL,
    duracao_confirmada_dias INTEGER NOT NULL,
    corrigido BOOLEAN NOT NULL,
    registrado_em TIMESTAMP NOT NULL
);
