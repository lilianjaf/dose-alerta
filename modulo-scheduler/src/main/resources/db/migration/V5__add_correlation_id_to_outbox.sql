ALTER TABLE outbox_event ADD COLUMN correlation_id VARCHAR(100);
ALTER TABLE evento_interacao_outbox ADD COLUMN correlation_id VARCHAR(100);
