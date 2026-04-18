package com.testgenieai.backend.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchemaCompatibilityInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureSchemaCompatibility() {
        List<String> statements = List.of(
                "ALTER TABLE app_users ADD COLUMN IF NOT EXISTS plan_tier VARCHAR(32) NOT NULL DEFAULT 'FREE'",
                "ALTER TABLE app_users ADD COLUMN IF NOT EXISTS subscription_status VARCHAR(32) NOT NULL DEFAULT 'TRIAL'",
                "ALTER TABLE app_users ADD COLUMN IF NOT EXISTS monthly_quota INTEGER NOT NULL DEFAULT 50",
                "ALTER TABLE app_users ADD COLUMN IF NOT EXISTS used_quota INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE app_users ADD COLUMN IF NOT EXISTS billing_cycle_start TIMESTAMP NOT NULL DEFAULT NOW()",
                "ALTER TABLE app_users ADD COLUMN IF NOT EXISTS billing_cycle_end TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '1 month')",
                "ALTER TABLE app_users ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(255)",
                "ALTER TABLE test_runs ADD COLUMN IF NOT EXISTS triggered_by VARCHAR(32) NOT NULL DEFAULT 'MANUAL'",
                "ALTER TABLE test_runs ADD COLUMN IF NOT EXISTS ci_project VARCHAR(255)",
                "ALTER TABLE test_runs ADD COLUMN IF NOT EXISTS ci_branch VARCHAR(255)",
                "ALTER TABLE test_runs ADD COLUMN IF NOT EXISTS ci_commit_sha VARCHAR(255)",
                "CREATE TABLE IF NOT EXISTS visual_baselines ("
                        + "id UUID PRIMARY KEY,"
                        + "owner_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,"
                        + "page_key VARCHAR(255) NOT NULL,"
                        + "baseline_hash VARCHAR(255) NOT NULL,"
                    + "baseline_image_path VARCHAR(1000),"
                        + "updated_at TIMESTAMP NOT NULL,"
                        + "CONSTRAINT uk_visual_baseline_owner_page UNIQUE (owner_id, page_key)"
                        + ")",
                "CREATE TABLE IF NOT EXISTS visual_comparisons ("
                        + "id UUID PRIMARY KEY,"
                        + "owner_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,"
                        + "test_run_id UUID REFERENCES test_runs(id) ON DELETE SET NULL,"
                        + "page_key VARCHAR(255) NOT NULL,"
                        + "baseline_hash VARCHAR(255),"
                    + "baseline_image_path VARCHAR(1000),"
                        + "current_hash VARCHAR(255) NOT NULL,"
                    + "current_image_path VARCHAR(1000) NOT NULL,"
                    + "diff_image_path VARCHAR(1000),"
                        + "diff_percent DOUBLE PRECISION NOT NULL,"
                        + "status VARCHAR(32) NOT NULL,"
                        + "created_at TIMESTAMP NOT NULL"
                        + ")",
                    "ALTER TABLE visual_baselines ADD COLUMN IF NOT EXISTS baseline_image_path VARCHAR(1000)",
                    "ALTER TABLE visual_comparisons ADD COLUMN IF NOT EXISTS baseline_image_path VARCHAR(1000)",
                    "ALTER TABLE visual_comparisons ADD COLUMN IF NOT EXISTS current_image_path VARCHAR(1000)",
                    "ALTER TABLE visual_comparisons ADD COLUMN IF NOT EXISTS diff_image_path VARCHAR(1000)",
                "CREATE INDEX IF NOT EXISTS idx_visual_baselines_owner_page ON visual_baselines(owner_id, page_key)",
                "CREATE INDEX IF NOT EXISTS idx_visual_comparisons_owner_created_at ON visual_comparisons(owner_id, created_at DESC)"
        );

        for (String statement : statements) {
            jdbcTemplate.execute(statement);
        }
    }
}
