INSERT INTO filter_configurations (filter_name, expected_insertions, false_positive_rate, tenant_id, rotatable, rotation_days, active)
VALUES 
    ('stolen-cards', 10000000, 0.001, 'default', false, null, true),
    ('duplicate-transactions', 50000000, 0.01, 'default', true, 7, true),
    ('invoice-payments', 5000000, 0.0001, 'default', false, null, true),
    ('suspicious-accounts', 1000000, 0.005, 'default', false, null, true)
ON CONFLICT (filter_name) DO NOTHING;

INSERT INTO api_keys (key_hash, tenant_id, name, rate_limit, active)
VALUES ('5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'default', 'Development Key', 10000, true)
ON CONFLICT (key_hash) DO NOTHING;
