-- Evita retentativa infinita: cada falha de publicação incrementa tentativas e agenda a próxima com espera
-- crescente; ao esgotar, o evento vira FALHOU (e eventos velhos demais viram EXPIRADO).
ALTER TABLE outbox_event ADD COLUMN tentativas INTEGER NOT NULL DEFAULT 0;
ALTER TABLE outbox_event ADD COLUMN proxima_tentativa_em TIMESTAMP;
