ALTER TABLE users
ADD COLUMN IF NOT EXISTS github_token TEXT,
ADD COLUMN IF NOT EXISTS facebook_token TEXT,
ADD COLUMN IF NOT EXISTS google_token TEXT,
ADD COLUMN IF NOT EXISTS vk_token TEXT;