ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS role VARCHAR(32) NOT NULL DEFAULT 'MEMBER';

UPDATE app_users
SET role = 'ADMIN'
WHERE email = 'system@testgenie.local';

ALTER TABLE app_users
    ALTER COLUMN role DROP DEFAULT;
