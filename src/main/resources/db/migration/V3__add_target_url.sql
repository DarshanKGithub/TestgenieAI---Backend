ALTER TABLE test_runs
    ADD COLUMN target_url VARCHAR(1000);

UPDATE test_runs
SET target_url = 'https://example.com'
WHERE target_url IS NULL;

ALTER TABLE test_runs
    ALTER COLUMN target_url SET NOT NULL;
