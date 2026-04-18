CREATE TABLE test_runs (
    id UUID PRIMARY KEY,
    suite_name VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    total_tests INTEGER NOT NULL,
    passed_tests INTEGER NOT NULL,
    failed_tests INTEGER NOT NULL,
    duration_ms BIGINT
);

CREATE TABLE test_case_results (
    id UUID PRIMARY KEY,
    test_run_id UUID NOT NULL,
    test_name VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    duration_ms BIGINT NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_test_case_results_run
        FOREIGN KEY (test_run_id)
        REFERENCES test_runs (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_test_case_results_run_id ON test_case_results(test_run_id);
CREATE INDEX idx_test_runs_started_at ON test_runs(started_at DESC);
