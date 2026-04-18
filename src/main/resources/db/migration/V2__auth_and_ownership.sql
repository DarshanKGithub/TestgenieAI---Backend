CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

INSERT INTO app_users (id, email, password_hash, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'system@testgenie.local', '$2a$10$3vCaU5TlQwM8B0u6JxVcAu4V6P0G9mDO54G2sF2vL8xOp9YyWdoqe', NOW())
ON CONFLICT (email) DO NOTHING;

ALTER TABLE test_runs
    ADD COLUMN owner_id UUID;

UPDATE test_runs
SET owner_id = '00000000-0000-0000-0000-000000000001'
WHERE owner_id IS NULL;

ALTER TABLE test_runs
    ALTER COLUMN owner_id SET NOT NULL;

ALTER TABLE test_runs
    ADD CONSTRAINT fk_test_runs_owner
    FOREIGN KEY (owner_id)
    REFERENCES app_users (id)
    ON DELETE CASCADE;

CREATE INDEX idx_test_runs_owner_id_started_at ON test_runs(owner_id, started_at DESC);
