CREATE TABLE IF NOT EXISTS api_keys (
    id BIGSERIAL PRIMARY KEY,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    tenant_id VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    rate_limit INTEGER NOT NULL DEFAULT 1000,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);
CREATE INDEX idx_api_keys_tenant_id ON api_keys(tenant_id);

CREATE TABLE IF NOT EXISTS filter_configurations (
    id BIGSERIAL PRIMARY KEY,
    filter_name VARCHAR(100) NOT NULL UNIQUE,
    expected_insertions BIGINT NOT NULL,
    false_positive_rate DOUBLE PRECISION NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    rotatable BOOLEAN NOT NULL DEFAULT false,
    rotation_days INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    filter_name VARCHAR(100) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    item_hash VARCHAR(64) NOT NULL,
    result VARCHAR(20) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    latency_micros BIGINT NOT NULL,
    trace_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_filter_name ON audit_logs(filter_name);
CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

CREATE TABLE IF NOT EXISTS bloom_filter_backup (
    id BIGSERIAL PRIMARY KEY,
    filter_name VARCHAR(100) NOT NULL,
    item_hash VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_backup_filter_name ON bloom_filter_backup(filter_name);
CREATE INDEX idx_backup_tenant_id ON bloom_filter_backup(tenant_id);
CREATE INDEX idx_backup_expires_at ON bloom_filter_backup(expires_at);

CREATE TABLE IF NOT EXISTS stolen_cards (
    id BIGSERIAL PRIMARY KEY,
    card_hash VARCHAR(64) NOT NULL UNIQUE,
    tenant_id VARCHAR(100) NOT NULL,
    reported_by VARCHAR(100),
    reported_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    verified_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_stolen_cards_hash ON stolen_cards(card_hash);
CREATE INDEX idx_stolen_cards_tenant_id ON stolen_cards(tenant_id);

CREATE TABLE IF NOT EXISTS recent_transactions (
    id BIGSERIAL PRIMARY KEY,
    fingerprint VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_recent_txn_fingerprint ON recent_transactions(fingerprint);
CREATE INDEX idx_recent_txn_tenant_id ON recent_transactions(tenant_id);
CREATE INDEX idx_recent_txn_created_at ON recent_transactions(created_at);
