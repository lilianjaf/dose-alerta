CREATE TABLE alarme (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    medicamento VARCHAR(255) NOT NULL,
    dose VARCHAR(100) NOT NULL,
    horario_alvo TIMESTAMP NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    etapa_atual VARCHAR(30)
);

CREATE INDEX idx_alarme_status ON alarme (status);

CREATE TABLE interacao (
    id UUID PRIMARY KEY,
    alarme_id UUID NOT NULL REFERENCES alarme (id),
    tipo VARCHAR(30) NOT NULL,
    registrada_em TIMESTAMP NOT NULL
);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    alarme_id UUID NOT NULL REFERENCES alarme (id),
    etapa VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    publicado_em TIMESTAMP
);

CREATE INDEX idx_outbox_event_status ON outbox_event (status);
