ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS plan_tier VARCHAR(32) NOT NULL DEFAULT 'FREE',
    ADD COLUMN IF NOT EXISTS subscription_status VARCHAR(32) NOT NULL DEFAULT 'TRIAL',
    ADD COLUMN IF NOT EXISTS monthly_quota INTEGER NOT NULL DEFAULT 50,
    ADD COLUMN IF NOT EXISTS used_quota INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS billing_cycle_start TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS billing_cycle_end TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '1 month'),
    ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(255);

ALTER TABLE test_runs
    ADD COLUMN IF NOT EXISTS triggered_by VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN IF NOT EXISTS ci_project VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ci_branch VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ci_commit_sha VARCHAR(255);

CREATE TABLE IF NOT EXISTS visual_baselines (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    page_key VARCHAR(255) NOT NULL,
    baseline_hash VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_visual_baselines_owner
        FOREIGN KEY (owner_id)
        REFERENCES app_users (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_visual_baseline_owner_page UNIQUE (owner_id, page_key)
);

CREATE TABLE IF NOT EXISTS visual_comparisons (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    test_run_id UUID,
    page_key VARCHAR(255) NOT NULL,
    baseline_hash VARCHAR(255),
    current_hash VARCHAR(255) NOT NULL,
    diff_percent DOUBLE PRECISION NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_visual_comparisons_owner
        FOREIGN KEY (owner_id)
        REFERENCES app_users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_visual_comparisons_run
        FOREIGN KEY (test_run_id)
        REFERENCES test_runs (id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_visual_baselines_owner_page ON visual_baselines(owner_id, page_key);
CREATE INDEX IF NOT EXISTS idx_visual_comparisons_owner_created_at ON visual_comparisons(owner_id, created_at DESC);
