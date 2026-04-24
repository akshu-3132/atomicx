-- Add idempotency_key column to transaction table for duplicate request prevention
ALTER TABLE transaction ADD COLUMN idempotency_key VARCHAR(64);

-- Create unique index on idempotency_key to enforce uniqueness and speed up lookups
CREATE UNIQUE INDEX idx_transaction_idempotency_key ON transaction(idempotency_key) WHERE idempotency_key IS NOT NULL;