-- Cloudflare D1 schema for Zovrax accounts. No static repository commits per favorite/view.
CREATE TABLE IF NOT EXISTS profiles (
  github_id INTEGER PRIMARY KEY,
  login TEXT NOT NULL,
  name TEXT NOT NULL DEFAULT '',
  avatar_url TEXT NOT NULL DEFAULT '',
  updated_at INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS sessions (
  token_hash TEXT PRIMARY KEY,
  github_id INTEGER NOT NULL REFERENCES profiles(github_id) ON DELETE CASCADE,
  expires_at INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS sessions_expiry ON sessions(expires_at);
CREATE TABLE IF NOT EXISTS tickets (
  ticket_hash TEXT PRIMARY KEY,
  github_id INTEGER NOT NULL REFERENCES profiles(github_id) ON DELETE CASCADE,
  expires_at INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS favorites (
  github_id INTEGER NOT NULL REFERENCES profiles(github_id) ON DELETE CASCADE,
  slug TEXT NOT NULL,
  PRIMARY KEY (github_id, slug)
);
CREATE INDEX IF NOT EXISTS favorite_slug ON favorites(slug);
CREATE TABLE IF NOT EXISTS mod_views (
  slug TEXT PRIMARY KEY,
  views INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS view_dedup (
  slug TEXT NOT NULL,
  day TEXT NOT NULL,
  fingerprint TEXT NOT NULL,
  PRIMARY KEY (slug, day, fingerprint)
);
-- Periodically delete old view_dedup records (older than 30 days).
