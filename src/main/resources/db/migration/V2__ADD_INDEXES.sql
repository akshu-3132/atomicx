
-- Create unique index on idempotency_key to enforce uniqueness and speed up lookups
CREATE UNIQUE INDEX idx_transaction_idempotency_key ON transaction(idempotency_key) WHERE idempotency_key IS NOT NULL;

-- 1. Optimize for fetching all entries for a specific transaction (Foreign Key lookup)
CREATE INDEX idx_ledger_transaction_id ON ledger_entry(transaction_id);

-- 2. Optimize for calculating total balance (Summing by account)
CREATE INDEX idx_ledger_account_id ON ledger_entry(account_id);

-- 3. Optimize for generating bank statements (Sorting recent history first)
CREATE INDEX idx_ledger_account_time ON ledger_entry(account_id, created_at DESC);