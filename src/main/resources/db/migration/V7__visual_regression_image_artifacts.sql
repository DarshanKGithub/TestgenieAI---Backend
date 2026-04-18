ALTER TABLE visual_baselines
    ADD COLUMN IF NOT EXISTS baseline_image_path VARCHAR(1000);

ALTER TABLE visual_comparisons
    ADD COLUMN IF NOT EXISTS baseline_image_path VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS current_image_path VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS diff_image_path VARCHAR(1000);
